package com.baize.flux.connector.jdbc.source;

import com.baize.flux.api.table.catalog.CatalogTable;
import com.baize.flux.api.table.catalog.TablePath;
import com.baize.flux.connector.jdbc.config.JdbcSourceConfig;
import com.baize.flux.connector.jdbc.core.dialect.JdbcDialect;
import com.baize.flux.connector.jdbc.core.dialect.JdbcDialectLoader;
import com.baize.flux.connector.jdbc.utils.JdbcCatalogUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Creates the bounded JDBC splits for a prepared source. */
final class JdbcSourceSplitGenerator {

    private JdbcSourceSplitGenerator() {
    }

    static List<JdbcSourceSplit> generate(
            JdbcSourceConfig config,
            Map<TablePath, CatalogTable> tables)
            throws Exception {

        Objects.requireNonNull(config, "config must not be null");
        Objects.requireNonNull(tables, "tables must not be null");

        JdbcDialect dialect = JdbcDialectLoader.load(config.getConnectionConfig());
        Map<TablePath, JdbcSourceTable> sourceTables =
                JdbcCatalogUtils.getTables(config, dialect);

        for (JdbcSourceTable sourceTable : sourceTables.values()) {
            if (!tables.containsKey(sourceTable.getTablePath())) {
                throw new IllegalArgumentException(
                        "No prepared table metadata for "
                                + sourceTable.getTablePath());
            }
        }

        return new JdbcSourceSplitEnumerator(config, sourceTables, 1)
                .enumerateSplits();
    }
}
