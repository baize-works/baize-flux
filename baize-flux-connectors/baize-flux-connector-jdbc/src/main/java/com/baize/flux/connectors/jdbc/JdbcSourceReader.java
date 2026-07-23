package com.baize.flux.connectors.jdbc;

import com.baize.flux.api.source.SourceReader;
import com.baize.flux.api.table.FluxRow;
import com.baize.flux.api.table.RecordBatch;
import com.baize.flux.api.table.RowType;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Reads one JDBC result set in finite batches. */
final class JdbcSourceReader implements SourceReader<FluxRow, JdbcSourceSplit> {
    private final String url;
    private final String query;
    private final String username;
    private final String password;
    private final String driver;
    private final int fetchSize;
    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;
    private RowType rowType;
    private boolean finished;

    JdbcSourceReader(String url, String query, String username, String password, String driver, int fetchSize) {
        this.url = url;
        this.query = query;
        this.username = username;
        this.password = password;
        this.driver = driver;
        this.fetchSize = fetchSize;
    }

    @Override
    public void open(JdbcSourceSplit split) throws Exception {
        if (connection != null) throw new IllegalStateException("Reader is already open");
        if (driver != null && !driver.trim().isEmpty()) Class.forName(driver);
        connection = DriverManager.getConnection(url, username, password);
        statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        statement.setFetchSize(fetchSize);
        resultSet = statement.executeQuery(query);
        ResultSetMetaData metadata = resultSet.getMetaData();
        List<String> fields = new ArrayList<String>(metadata.getColumnCount());
        for (int index = 1; index <= metadata.getColumnCount(); index++) fields.add(metadata.getColumnLabel(index));
        rowType = new RowType(fields);
    }

    @Override
    public RecordBatch<FluxRow> pollBatch() throws Exception {
        ensureOpen();
        if (finished) return RecordBatch.endOfInput();
        List<FluxRow> rows = new ArrayList<FluxRow>(fetchSize);
        while (rows.size() < fetchSize && resultSet.next()) {
            List<Object> values = new ArrayList<Object>(rowType.fieldCount());
            for (int index = 1; index <= rowType.fieldCount(); index++) values.add(resultSet.getObject(index));
            rows.add(new FluxRow(rowType, values));
        }
        if (rows.isEmpty()) finished = true;
        return rows.isEmpty() ? RecordBatch.<FluxRow>endOfInput() : RecordBatch.of(rows);
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public void close() throws Exception {
        Exception failure = null;
        try { if (resultSet != null) resultSet.close(); } catch (Exception e) { failure = e; }
        try { if (statement != null) statement.close(); } catch (Exception e) { if (failure == null) failure = e; else failure.addSuppressed(e); }
        try { if (connection != null) connection.close(); } catch (Exception e) { if (failure == null) failure = e; else failure.addSuppressed(e); }
        resultSet = null;
        statement = null;
        connection = null;
        if (failure != null) throw failure;
    }

    private void ensureOpen() {
        if (resultSet == null) throw new IllegalStateException("Reader is not open");
    }
}
