package com.baize.flux.connector.jdbc.core.split;

import com.baize.flux.api.table.catalog.TablePath;
import com.baize.flux.connector.jdbc.config.JdbcSourceConfig;
import com.baize.flux.connector.jdbc.core.dialect.JdbcDialect;
import com.baize.flux.connector.jdbc.core.dialect.JdbcDialectLoader;
import com.baize.flux.connector.jdbc.source.JdbcSourceSplit;
import com.baize.flux.connector.jdbc.source.JdbcSourceTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Plans the static, table-level splits used by the bounded JDBC source. */
public final class JdbcSplitPlanner {

    private JdbcSplitPlanner() {}

    public static List<JdbcSourceSplit> plan(
            JdbcSourceConfig config,
            Map<TablePath, JdbcSourceTable> sourceTables,
            int parallelism) {

        Objects.requireNonNull(config, "config must not be null");
        Objects.requireNonNull(sourceTables, "sourceTables must not be null");

        if (parallelism <= 0) {
            throw new IllegalArgumentException("parallelism must be greater than 0");
        }

        JdbcDialect dialect = JdbcDialectLoader.load(config.getConnectionConfig());
        List<JdbcSourceSplit> splits = new ArrayList<>(sourceTables.size());

        for (JdbcSourceTable table : sourceTables.values()) {
            String query = dialect.buildSelectSql(table, config.getWhereCondition());
            String splitId = table.getTablePath() + "-0";
            splits.add(new JdbcSourceSplit(
                    table.getTablePath(), splitId, query, null, null, null, null));
        }

        return Collections.unmodifiableList(splits);
    }
}
