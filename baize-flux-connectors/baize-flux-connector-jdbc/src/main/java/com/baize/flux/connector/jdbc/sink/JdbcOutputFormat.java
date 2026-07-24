package com.baize.flux.connector.jdbc.sink;

import com.baize.flux.api.table.catalog.Catalog;
import com.baize.flux.api.table.catalog.CatalogTable;
import com.baize.flux.api.table.catalog.PrimaryKey;
import com.baize.flux.api.table.catalog.TablePath;
import com.baize.flux.api.table.catalog.TableSchema;
import com.baize.flux.api.table.catalog.WritableCatalog;
import com.baize.flux.api.table.type.FluxRow;
import com.baize.flux.connector.jdbc.config.JdbcSinkConfig;
import com.baize.flux.connector.jdbc.core.dialect.JdbcDialect;
import com.baize.flux.connector.jdbc.core.dialect.JdbcDialectLoader;
import com.baize.flux.connector.jdbc.internal.JdbcConnectionProvider;
import com.baize.flux.connector.jdbc.sink.savemode.JdbcSaveModeHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Savepoint;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JDBC 批量输出组件。
 *
 * <p>一个 JdbcOutputFormat 对应一个 SinkTask，
 * 同一个 SinkTask 的所有 DML 使用同一条 JDBC 事务。
 *
 * <p>事务生命周期：
 *
 * <pre>
 * open
 *   -> write batch 1
 *   -> write batch 2
 *   -> commit
 *
 * 发生异常：
 *
 * open
 *   -> write batch 1
 *   -> write batch 2 failed
 *   -> rollback
 * </pre>
 */
public final class JdbcOutputFormat
        implements AutoCloseable {

    private final JdbcSinkConfig config;

    private final JdbcDialect dialect;

    private final JdbcConnectionProvider connectionProvider;

    /**
     * 已经完成 SaveMode 处理的目标表。
     */
    private final Set<TablePath> preparedTables =
            new HashSet<TablePath>();

    /** Rows skipped after the failed batch has been isolated to individual rows. */
    private final List<JdbcRowError> rowErrors =
            new ArrayList<JdbcRowError>();

    private Connection connection;

    private Boolean supportsSavepoints;

    private boolean opened;

    private boolean transactionCompleted;

    private boolean closed;

    JdbcOutputFormat(
            JdbcSinkConfig config) {

        this.config =
                Objects.requireNonNull(
                        config,
                        "config must not be null");

        this.dialect =
                JdbcDialectLoader.load(
                        config.getConnectionConfig());

        this.connectionProvider =
                new JdbcConnectionProvider(
                        config.getConnectionConfig(),
                        dialect);
    }

    /**
     * 初始化输出组件。
     *
     * <p>这里暂时不立即创建连接，避免空任务无意义地连接数据库。
     * 第一批数据写入时才真正开启事务。
     */
    public void open() {
        if (closed) {
            throw new IllegalStateException(
                    "JdbcOutputFormat has already been closed");
        }

        if (opened) {
            throw new IllegalStateException(
                    "JdbcOutputFormat has already been opened");
        }

        opened = true;
    }

    /**
     * 写入一批数据。
     *
     * <p>这里只执行 SQL，不提交事务。
     * 最终提交由 commit 方法统一完成。
     */
    public void write(
            List<FluxRow> rows,
            CatalogTable sourceTable)
            throws Exception {

        checkWritable();

        if (rows == null || rows.isEmpty()) {
            return;
        }

        Objects.requireNonNull(
                sourceTable,
                "sourceTable must not be null");

        CatalogTable targetTable =
                resolveTargetTable(sourceTable);

        /*
         * SaveMode 和建表操作发生在数据写入前。
         */
        prepareTable(targetTable);

        ensureTransactionConnection();

        List<String> fields =
                targetTable
                        .getTableSchema()
                        .getColumns()
                        .stream()
                        .map(column -> column.getName())
                        .collect(Collectors.toList());

        String sql =
                createWriteSql(
                        targetTable,
                        fields);

        for (int start = 0;
             start < rows.size();
             start += config.getBatchSize()) {

            int end =
                    Math.min(
                            start + config.getBatchSize(),
                            rows.size());

            executeBatch(
                    sql,
                    rows.subList(start, end),
                    targetTable.getTableSchema());
        }
    }

    /**
     * 提交当前 SinkTask 的完整事务。
     */
    public void commit() throws Exception {
        checkOpened();

        if (transactionCompleted) {
            return;
        }

        /*
         * 空任务可能从未创建 JDBC 连接。
         */
        if (connection != null) {
            connection.commit();
        }

        transactionCompleted = true;
    }

    /**
     * 回滚当前 SinkTask 的完整事务。
     */
    public void rollback() throws Exception {
        if (!opened || transactionCompleted) {
            return;
        }

        Exception rollbackFailure = null;

        try {
            if (connection != null) {
                connection.rollback();
            }
        } catch (Exception exception) {
            rollbackFailure = exception;
        } finally {
            transactionCompleted = true;
        }

        if (rollbackFailure != null) {
            throw rollbackFailure;
        }
    }

    /**
     * Returns a snapshot of rows rejected while {@code dirty_data_policy=SKIP}.
     */
    public List<JdbcRowError> getRowErrors() {
        return Collections.unmodifiableList(
                new ArrayList<JdbcRowError>(rowErrors));
    }

    /**
     * 执行一个 JDBC Batch。
     *
     * <p>整个 SinkTask 共用一条事务，因此批次重试不能执行完整 rollback。
     * 这里通过 Savepoint 只回滚当前失败批次。
     */
    private void executeBatch(
            String sql,
            List<FluxRow> rows,
            TableSchema schema)
            throws Exception {

        ensureTransactionConnection();

        try {
            executeRows(sql, rows, schema);
        } catch (Exception batchFailure) {
            if (!config.shouldSkipDirtyData()) {
                throw batchFailure;
            }

            if (!isTransactionConnectionValid()) {
                throw batchFailure;
            }

            /*
             * The failed batch has already been rolled back to its savepoint.
             * Replay it one row at a time so valid rows remain in the task
             * transaction and only the rows that still fail are discarded.
             */
            for (FluxRow row : rows) {
                try {
                    executeRows(sql, Collections.singletonList(row), schema);
                } catch (Exception rowFailure) {
                    if (!isTransactionConnectionValid()) {
                        throw rowFailure;
                    }
                    rowErrors.add(new JdbcRowError(row, rowFailure));
                }
            }
        }
    }

    private void executeRows(
            String sql,
            List<FluxRow> rows,
            TableSchema schema)
            throws Exception {

        int maxRetries = config.getMaxRetries();
        boolean requiresSavepoint = maxRetries > 0 || config.shouldSkipDirtyData();

        if (requiresSavepoint
                && !supportsSavepoints()) {

            throw new IllegalStateException(
                    "当前 JDBC 数据库或驱动不支持 Savepoint，"
                            + "无法在任务级事务中安全执行批次重试或脏数据隔离。"
                            + "请关闭重试和脏数据跳过，"
                            + "或者使用支持 Savepoint 的 JDBC 驱动");
        }

        Exception lastFailure = null;

        for (int attempt = 0;
             attempt <= maxRetries;
             attempt++) {

            Savepoint savepoint = null;

            /*
             * 配置了批次重试时，为当前批次创建保存点。
             */
            if (requiresSavepoint) {
                savepoint =
                        connection.setSavepoint(
                                "baize_flux_batch_"
                                        + attempt);
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                for (FluxRow row : rows) {
                    dialect.rowConverter()
                            .write(
                                    statement,
                                    row,
                                    schema);

                    statement.addBatch();
                }

                statement.executeBatch();

                releaseSavepointQuietly(
                        savepoint,
                        null);

                return;

            } catch (Exception exception) {
                lastFailure = exception;

                /*
                 * 没有 Savepoint 时不能在当前事务内安全重试。
                 * 将异常交给 SinkTask，由 SinkTask 回滚整个事务。
                 */
                if (savepoint == null) {
                    throw exception;
                }

                try {
                    connection.rollback(savepoint);
                } catch (Exception rollbackFailure) {
                    exception.addSuppressed(
                            rollbackFailure);

                    throw exception;
                } finally {
                    releaseSavepointQuietly(
                            savepoint,
                            exception);
                }

                /*
                 * 事务连接已经断开时，原事务已经无法继续。
                 *
                 * 不能重新建立连接后继续写，因为新连接不包含前面批次
                 * 尚未提交的事务数据。
                 */
                if (!isTransactionConnectionValid()) {
                    SQLException connectionFailure =
                            new SQLException(
                                    "JDBC transaction connection was lost; "
                                            + "the whole SinkTask must be rolled back "
                                            + "and restarted",
                                    exception);

                    throw connectionFailure;
                }

                if (attempt >= maxRetries) {
                    throw exception;
                }
            }
        }

        throw lastFailure == null
                ? new IllegalStateException(
                "JDBC batch execution failed")
                : lastFailure;
    }

    /**
     * 准备目标表。
     *
     * <p>只有 SaveMode 完整执行成功后，才把目标表加入 preparedTables。
     * 避免准备失败后，下一次调用错误地跳过该目标表。
     */
    private void prepareTable(
            CatalogTable targetTable)
            throws Exception {

        TablePath tablePath =
                targetTable.getTablePath();

        if (preparedTables.contains(tablePath)) {
            return;
        }

        Catalog catalog =
                dialect.createCatalog(
                        config.getConnectionConfig());

        if (!(catalog instanceof WritableCatalog)) {
            throw new IllegalStateException(
                    "JDBC catalog does not support DDL: "
                            + dialect.name());
        }

        JdbcSaveModeHandler saveModeHandler =
                new JdbcSaveModeHandler(
                        config.getSchemaSaveMode(),
                        config.getDataSaveMode(),
                        (WritableCatalog) catalog,
                        targetTable,
                        config.isCreatePrimaryKey());

        try {
            saveModeHandler.open();
            saveModeHandler.handleSaveMode();

            /*
             * 只有处理成功以后才记录。
             */
            preparedTables.add(tablePath);

        } finally {
            saveModeHandler.close();
        }
    }

    /**
     * 生成目标端写入 SQL。
     */
    private String createWriteSql(
            CatalogTable targetTable,
            List<String> fields) {

        if (config.hasCustomSql()) {
            return config.getCustomSql();
        }

        if (config.isUpsert()) {
            return dialect
                    .buildUpsertSql(
                            targetTable.getTablePath(),
                            fields,
                            resolvePrimaryKeys(targetTable))
                    .orElseThrow(
                            () -> new IllegalArgumentException(
                                    "Dialect does not support UPSERT: "
                                            + dialect.name()));
        }

        return dialect.buildInsertSql(
                targetTable.getTablePath(),
                fields);
    }

    /**
     * 将源表映射为目标表。
     */
    private CatalogTable resolveTargetTable(
            CatalogTable sourceTable) {

        String targetTablePath =
                config.resolveTargetTablePath(
                        sourceTable.getTablePath());

        TablePath resolvedPath =
                targetTablePath == null
                        ? sourceTable.getTablePath()
                        : dialect.parseTablePath(
                        targetTablePath);

        return sourceTable.withPath(
                resolvedPath);
    }

    /**
     * 获取 Upsert 使用的主键字段。
     */
    private List<String> resolvePrimaryKeys(
            CatalogTable table) {

        if (config.hasConfiguredPrimaryKeys()) {
            return config.getPrimaryKeys();
        }

        PrimaryKey primaryKey =
                table.getTableSchema()
                        .getPrimaryKey();

        if (primaryKey == null) {
            return new ArrayList<String>();
        }

        return primaryKey.getColumnNames();
    }

    /**
     * 获取或检查当前事务连接。
     *
     * <p>事务已经开始后，不允许自动重连并继续写入。
     * 因为新连接无法继承原连接中尚未提交的数据。
     */
    private void ensureTransactionConnection()
            throws Exception {

        if (connection == null) {
            connection =
                    connectionProvider
                            .getOrEstablishConnection();

            connection.setAutoCommit(false);

            return;
        }

        if (!isTransactionConnectionValid()) {
            throw new SQLException(
                    "JDBC transaction connection is invalid; "
                            + "the transaction cannot continue");
        }
    }

    private boolean isTransactionConnectionValid() {
        try {
            return connection != null
                    && !connection.isClosed()
                    && connectionProvider
                    .isConnectionValid();
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean supportsSavepoints()
            throws SQLException {

        if (supportsSavepoints == null) {
            supportsSavepoints =
                    connection
                            .getMetaData()
                            .supportsSavepoints();
        }

        return supportsSavepoints;
    }

    private void releaseSavepointQuietly(
            Savepoint savepoint,
            Exception originalFailure) {

        if (savepoint == null
                || connection == null) {
            return;
        }

        try {
            connection.releaseSavepoint(
                    savepoint);
        } catch (Exception releaseFailure) {
            /*
             * releaseSavepoint 失败通常不影响事务正确性。
             * 如果当前已经有主异常，则保留为 suppressed。
             */
            if (originalFailure != null) {
                originalFailure.addSuppressed(
                        releaseFailure);
            }
        }
    }

    private void checkOpened() {
        if (!opened) {
            throw new IllegalStateException(
                    "JdbcOutputFormat has not been opened");
        }

        if (closed) {
            throw new IllegalStateException(
                    "JdbcOutputFormat has already been closed");
        }
    }

    private void checkWritable() {
        checkOpened();

        if (transactionCompleted) {
            throw new IllegalStateException(
                    "JDBC transaction has already been completed");
        }
    }

    /**
     * 关闭连接。
     *
     * <p>如果调用方忘记 commit 或 rollback，
     * close 会尽量回滚尚未完成的事务，防止意外提交。
     */
    @Override
    public void close() throws Exception {
        if (closed) {
            return;
        }

        closed = true;

        Exception failure = null;

        if (opened && !transactionCompleted) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (Exception rollbackFailure) {
                failure = rollbackFailure;
            } finally {
                transactionCompleted = true;
            }
        }

        try {
            connectionProvider.closeConnection();
        } catch (Exception closeFailure) {
            if (failure == null) {
                failure = closeFailure;
            } else {
                failure.addSuppressed(
                        closeFailure);
            }
        } finally {
            connection = null;
        }

        if (failure != null) {
            throw failure;
        }
    }
}
