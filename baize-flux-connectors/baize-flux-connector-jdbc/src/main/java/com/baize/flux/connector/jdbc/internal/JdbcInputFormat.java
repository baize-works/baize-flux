package com.baize.flux.connector.jdbc.internal;

import com.baize.flux.api.table.catalog.CatalogTable;
import com.baize.flux.api.table.catalog.TablePath;
import com.baize.flux.api.table.type.FluxRow;
import com.baize.flux.connector.jdbc.config.JdbcSourceConfig;
import com.baize.flux.connector.jdbc.core.dialect.JdbcDialect;
import com.baize.flux.connector.jdbc.core.dialect.JdbcDialectLoader;
import com.baize.flux.connector.jdbc.source.JdbcSourceSplit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.Objects;

/** Owns the JDBC resources required to read one source split at a time. */
public final class JdbcInputFormat {

    private final JdbcSourceConfig config;
    private final Map<TablePath, CatalogTable> tables;
    private final JdbcDialect dialect;
    private final JdbcConnectionProvider connectionProvider;
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private CatalogTable currentTable;
    private boolean exhausted;

    public JdbcInputFormat(JdbcSourceConfig config, Map<TablePath, CatalogTable> tables) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.tables = Objects.requireNonNull(tables, "tables must not be null");
        this.dialect = JdbcDialectLoader.load(config.getConnectionConfig());
        this.connectionProvider = new JdbcConnectionProvider(config.getConnectionConfig(), dialect);
    }

    public void openInputFormat() throws Exception {
        connection = connectionProvider.getOrEstablishConnection();
    }

    public void open(JdbcSourceSplit split) throws Exception {
        if (connection == null) {
            throw new IllegalStateException("JdbcInputFormat has not been opened");
        }
        close();
        currentTable = tables.get(split.getTablePath());
        if (currentTable == null) {
            throw new IllegalArgumentException("No table metadata for split: " + split.splitId());
        }
        statement = dialect.prepareReadStatement(connection, split.getSplitQuery(), config.getFetchSize());
        resultSet = statement.executeQuery();
        exhausted = false;
    }

    public boolean reachedEnd() throws Exception {
        return resultSet == null || exhausted;
    }

    public FluxRow nextRecord() throws Exception {
        if (resultSet == null || !resultSet.next()) {
            exhausted = true;
            return null;
        }
        return dialect.rowConverter().read(resultSet, currentTable.getTableSchema());
    }

    public void close() throws Exception {
        if (resultSet != null) { resultSet.close(); resultSet = null; }
        if (statement != null) { statement.close(); statement = null; }
        currentTable = null;
        exhausted = false;
    }

    public void closeInputFormat() throws Exception {
        close();
        connectionProvider.closeConnection();
        connection = null;
    }
}
