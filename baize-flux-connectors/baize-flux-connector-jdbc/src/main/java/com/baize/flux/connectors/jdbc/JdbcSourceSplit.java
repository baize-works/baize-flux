package com.baize.flux.connectors.jdbc;

import com.baize.flux.api.source.SourceSplit;

final class JdbcSourceSplit implements SourceSplit {
    private final String splitId;

    JdbcSourceSplit(String splitId) {
        this.splitId = splitId;
    }

    @Override
    public String splitId() {
        return splitId;
    }
}
