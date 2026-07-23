package com.baize.flux.connector.jdbc.sink;

import com.baize.flux.api.table.catalog.*;
import com.baize.flux.api.table.catalog.exception.TableNotFoundException;
import com.baize.flux.api.table.type.FluxRow;
import com.baize.flux.connector.jdbc.config.JdbcSinkConfig;
import com.baize.flux.connector.jdbc.core.dialect.JdbcDialect;
import com.baize.flux.connector.jdbc.core.dialect.JdbcDialectLoader;
import com.baize.flux.connector.jdbc.internal.JdbcConnectionProvider;
import com.baize.flux.connector.jdbc.sink.savemode.JdbcSaveModeHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JDBC batch output with target-table preparation and explicit transactions.
 */
public final class JdbcOutputFormat implements AutoCloseable {
    private final JdbcSinkConfig config;
    private final JdbcDialect dialect;
    private final JdbcConnectionProvider connectionProvider;
    private final Set<TablePath> preparedTables = new HashSet<>();
    private Connection connection;

    JdbcOutputFormat(JdbcSinkConfig config) {
        this.config = config;
        dialect = JdbcDialectLoader.load(config.getConnectionConfig());
        connectionProvider = new JdbcConnectionProvider(config.getConnectionConfig(), dialect);
    }

    public void write(List<FluxRow> rows, CatalogTable sourceTable) throws Exception {
        if (rows == null || rows.isEmpty()) return;
        CatalogTable target = targetTable(sourceTable);
        prepareTable(target);
        ensureConnection();
        List<String> fields = target.getTableSchema().getColumns().stream().map(c -> c.getName()).collect(Collectors.toList());
        String sql = config.hasCustomSql() ? config.getCustomSql() : config.isUpsert() ? dialect.buildUpsertSql(target.getTablePath(), fields, primaryKeys(target)).orElseThrow(() -> new IllegalArgumentException("Dialect does not support UPSERT: " + dialect.name())) : dialect.buildInsertSql(target.getTablePath(), fields);
        for (int start = 0; start < rows.size(); start += config.getBatchSize()) {
            int end = Math.min(start + config.getBatchSize(), rows.size());
            executeBatch(sql, rows.subList(start, end), target.getTableSchema());
        }
    }

    private void executeBatch(String sql, List<FluxRow> rows, TableSchema schema) throws Exception {
        Exception failure = null;
        for (int attempt = 0; attempt <= config.getMaxRetries(); attempt++) {
            try {
                ensureConnection();
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    for (FluxRow row : rows) {
                        dialect.rowConverter().write(statement, row, schema);
                        statement.addBatch();
                    }
                    statement.executeBatch();
                    connection.commit();
                    return;
                }
            } catch (Exception e) {
                failure = e;
                rollback(e);
                if (!connectionProvider.isConnectionValid()) {
                    connection = connectionProvider.reestablishConnection();
                    connection.setAutoCommit(false);
                }
            }
        }
        throw failure;
    }

    private void prepareTable(CatalogTable target) throws Exception {
        if (!preparedTables.add(target.getTablePath())) return;
        Catalog catalog = dialect.createCatalog(config.getConnectionConfig());
        if (!(catalog instanceof WritableCatalog))
            throw new IllegalStateException("JDBC catalog does not support DDL");
        JdbcSaveModeHandler saveModeHandler =
                new JdbcSaveModeHandler(
                        config.getSchemaSaveMode(),
                        config.getDataSaveMode(),
                        (WritableCatalog) catalog,
                        target,
                        config.isCreatePrimaryKey());
        try {
            saveModeHandler.open();
            saveModeHandler.handleSaveMode();
        } finally {
            saveModeHandler.close();
        }
    }

    private CatalogTable targetTable(CatalogTable source) {
        String targetTablePath = config.resolveTargetTablePath(source.getTablePath());
        TablePath path =
                targetTablePath == null
                        ? source.getTablePath()
                        : dialect.parseTablePath(targetTablePath);
        return source.withPath(path);
    }

    private List<String> primaryKeys(CatalogTable table) {
        if (config.hasConfiguredPrimaryKeys()) return config.getPrimaryKeys();
        PrimaryKey key = table.getTableSchema().getPrimaryKey();
        return key == null ? new ArrayList<>() : key.getColumnNames();
    }

    private void ensureConnection() throws Exception {
        if (connection == null || !connectionProvider.isConnectionValid()) {
            connection = connectionProvider.getOrEstablishConnection();
            connection.setAutoCommit(false);
        }
    }

    private void rollback(Exception original) {
        try {
            if (connection != null) connection.rollback();
        } catch (Exception e) {
            original.addSuppressed(e);
        }
    }

    @Override
    public void close() {
        connectionProvider.closeConnection();
        connection = null;
    }
}
