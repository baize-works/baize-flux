package com.baize.flux.connectors.jdbc;

import com.baize.flux.api.configuration.*;
import com.baize.flux.api.factory.SourceFactory;
import com.baize.flux.api.source.BoundedSource;
import com.baize.flux.api.table.FluxRow;

public final class JdbcSourceFactory implements SourceFactory {
    public String factoryIdentifier() {
        return "jdbc";
    }

    public OptionRule optionRule() {
        return JdbcOptions.sourceRule();
    }

    public BoundedSource<FluxRow, ?> createSource(ReadonlyConfig c) {
        return new JdbcSource(c.get(JdbcOptions.URL), c.get(JdbcOptions.USERNAME), c.get(JdbcOptions.PASSWORD), c.get(JdbcOptions.QUERY), c.get(JdbcOptions.FETCH_SIZE));
    }
}
