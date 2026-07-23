package com.baize.flux.connectors.jdbc;

import com.baize.flux.api.source.BoundedSource;
import com.baize.flux.api.source.SourceReader;
import com.baize.flux.api.table.FluxRow;

import java.util.Collections;
import java.util.List;

/** A single-query, single-split JDBC source. */
final class JdbcSource implements BoundedSource<FluxRow, JdbcSourceSplit> {
    private final String url;
    private final String query;
    private final String username;
    private final String password;
    private final String driver;
    private final int fetchSize;

    JdbcSource(String url, String query, String username, String password, String driver, int fetchSize) {
        this.url = url;
        this.query = query;
        this.username = username;
        this.password = password;
        this.driver = driver;
        this.fetchSize = fetchSize;
    }

    @Override
    public List<JdbcSourceSplit> planSplits(int parallelism) {
        if (parallelism < 1) throw new IllegalArgumentException("parallelism must be at least 1");
        return Collections.singletonList(new JdbcSourceSplit("jdbc-query"));
    }

    @Override
    public SourceReader<FluxRow, JdbcSourceSplit> createReader() {
        return new JdbcSourceReader(url, query, username, password, driver, fetchSize);
    }
}
