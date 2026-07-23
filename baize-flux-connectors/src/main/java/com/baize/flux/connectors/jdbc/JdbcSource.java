package com.baize.flux.connectors.jdbc;

import com.baize.flux.api.source.*;
import com.baize.flux.api.table.*;

import java.sql.*;
import java.util.*;

final class JdbcSource implements BoundedSource<FluxRow, JdbcSource.Split> {
    private final String url, user, password, query;
    private final int fetchSize;

    JdbcSource(String u, String n, String p, String q, int f) {
        url = u;
        user = n;
        password = p;
        query = q;
        fetchSize = f;
    }

    public List<Split> planSplits(int p) {
        return Collections.singletonList(new Split("jdbc-query"));
    }

    public SourceReader<FluxRow, Split> createReader() {
        return new Reader();
    }

    static final class Split implements SourceSplit {
        private final String id;

        Split(String i) {
            id = i;
        }

        public String splitId() {
            return id;
        }
    }

    private final class Reader implements SourceReader<FluxRow, Split> {
        private Connection connection;
        private Statement statement;
        private ResultSet resultSet;
        private RowType rowType;
        private boolean finished;

        public void open(Split s) throws SQLException {
            connection = DriverManager.getConnection(url, user, password);
            statement = connection.createStatement();
            statement.setFetchSize(fetchSize);
            resultSet = statement.executeQuery(query);
            ResultSetMetaData meta = resultSet.getMetaData();
            List<String> names = new ArrayList<String>();
            for (int i = 1; i <= meta.getColumnCount(); i++) names.add(meta.getColumnLabel(i));
            rowType = new RowType(names);
        }

        public RecordBatch<FluxRow> pollBatch() throws SQLException {
            if (finished) return RecordBatch.endOfInput();
            List<FluxRow> rows = new ArrayList<FluxRow>();
            while (rows.size() < fetchSize && resultSet.next()) {
                List<Object> values = new ArrayList<Object>();
                for (int i = 1; i <= rowType.fieldCount(); i++) values.add(resultSet.getObject(i));
                rows.add(new FluxRow(rowType, values));
            }
            if (rows.isEmpty()) finished = true;
            return RecordBatch.of(rows);
        }

        public boolean isFinished() {
            return finished;
        }

        public void close() throws SQLException {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
            if (connection != null) connection.close();
        }
    }
}
