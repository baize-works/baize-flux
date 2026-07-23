package com.baize.flux.connector.jdbc.core.dialect.mysql;

import com.baize.flux.api.table.catalog.Catalog;
import com.baize.flux.api.table.catalog.TablePath;
import com.baize.flux.connector.jdbc.catalog.mysql.MySqlCatalog;
import com.baize.flux.connector.jdbc.config.JdbcConnectionConfig;
import com.baize.flux.connector.jdbc.core.converter.JdbcRowConverter;
import com.baize.flux.connector.jdbc.core.dialect.DatabaseIdentifier;
import com.baize.flux.connector.jdbc.core.dialect.JdbcDialect;
import com.baize.flux.connector.jdbc.core.dialect.JdbcTypeMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * MySQL 离线 JDBC 方言。
 */
public final class MySqlDialect
        implements JdbcDialect {

    private final JdbcConnectionConfig connectionConfig;
    private final MySqlTypeMapper typeMapper;

    public MySqlDialect(
            JdbcConnectionConfig connectionConfig) {

        if (connectionConfig == null) {
            throw new IllegalArgumentException(
                    "connectionConfig must not be null");
        }

        this.connectionConfig =
                connectionConfig;

        this.typeMapper =
                new MySqlTypeMapper(
                        connectionConfig
                                .isIntTypeNarrowing());
    }

    @Override
    public String name() {
        return DatabaseIdentifier.MYSQL;
    }

    @Override
    public Catalog createCatalog(
            String catalogName,
            JdbcConnectionConfig connectionConfig) {

        return new MySqlCatalog(
                catalogName,
                connectionConfig);
    }

    @Override
    public JdbcTypeMapper typeMapper() {
        return typeMapper;
    }

    @Override
    public JdbcRowConverter rowConverter() {
        return new MySqlJdbcRowConverter();
    }

    @Override
    public TablePath parseTablePath(
            String tablePath) {

        return TablePath.parse(tablePath);
    }

    @Override
    public String quoteIdentifier(
            String identifier) {

        if (!JdbcDialect.hasText(identifier)) {
            throw new IllegalArgumentException(
                    "identifier must not be empty");
        }

        return "`"
                + identifier.trim()
                        .replace("`", "``")
                + "`";
    }

    @Override
    public String tableIdentifier(
            TablePath tablePath) {

        String database =
                tablePath.getDatabaseName();

        if (!JdbcDialect.hasText(database)) {
            database =
                    connectionConfig
                            .getDefaultDatabase()
                            .orElse(null);
        }

        if (!JdbcDialect.hasText(database)) {
            throw new IllegalArgumentException(
                    "MySQL 表路径缺少 database："
                            + tablePath);
        }

        return quoteIdentifier(database)
                + "."
                + quoteIdentifier(
                        tablePath.getTableName());
    }

    @Override
    public Optional<String> buildUpsertSql(
            TablePath tablePath,
            List<String> fieldNames,
            List<String> primaryKeys) {

        JdbcDialect.validateFields(fieldNames);

        Set<String> primaryKeySet =
                JdbcDialect.normalizeFields(
                        primaryKeys);

        if (primaryKeySet.isEmpty()) {
            throw new IllegalArgumentException(
                    "MySQL UPSERT 必须配置主键字段");
        }

        List<String> updateFields =
                fieldNames.stream()
                        .filter(
                                field ->
                                        !primaryKeySet.contains(
                                                field))
                        .collect(Collectors.toList());

        /*
         * 当所有字段都是主键时，使用第一个主键进行无操作更新，
         * 保证 SQL 语法合法。
         */
        if (updateFields.isEmpty()) {
            updateFields =
                    Collections.singletonList(
                            primaryKeys.get(0));
        }

        String updateClause =
                updateFields.stream()
                        .map(
                                field ->
                                        quoteIdentifier(field)
                                                + " = VALUES("
                                                + quoteIdentifier(field)
                                                + ")")
                        .collect(Collectors.joining(", "));

        return Optional.of(
                buildInsertSql(
                                tablePath,
                                fieldNames)
                        + " ON DUPLICATE KEY UPDATE "
                        + updateClause);
    }

    @Override
    public PreparedStatement prepareReadStatement(
            Connection connection,
            String sql,
            int fetchSize)
            throws SQLException {

        PreparedStatement statement =
                connection.prepareStatement(
                        sql,
                        ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_READ_ONLY);

        if (fetchSize > 0) {
            statement.setFetchSize(fetchSize);
        }

        return statement;
    }

    @Override
    public Map<String, String>
            defaultConnectionProperties() {

        Map<String, String> result =
                new LinkedHashMap<>();

        result.put(
                "rewriteBatchedStatements",
                "true");

        /*
         * 配合正数 fetchSize 使用服务端游标读取大表。
         * 用户可通过 properties 覆盖。
         */
        result.put(
                "useCursorFetch",
                "true");

        /*
         * tinyint(1) 是否映射 Boolean 由 MySqlTypeMapper 决定。
         */
        result.put(
                "tinyInt1isBit",
                "false");

        return Collections.unmodifiableMap(result);
    }
}
