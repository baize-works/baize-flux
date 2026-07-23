package com.baize.flux.connector.jdbc.core.split;

import com.baize.flux.api.table.catalog.TablePath;
import com.baize.flux.api.table.catalog.Column;
import com.baize.flux.api.table.type.FluxDataType;
import com.baize.flux.connector.jdbc.config.JdbcSourceConfig;
import com.baize.flux.connector.jdbc.core.dialect.JdbcDialect;
import com.baize.flux.connector.jdbc.core.dialect.JdbcDialectLoader;
import com.baize.flux.connector.jdbc.source.JdbcSourceSplit;
import com.baize.flux.connector.jdbc.source.JdbcSourceTable;

import java.util.*;
import java.math.BigDecimal;

/**
 * Plans the static, table-level splits used by the bounded JDBC source.
 */
public final class JdbcSplitPlanner {

    private JdbcSplitPlanner() {
    }

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
            if (!hasText(table.getPartitionColumn())) {
                splits.add(split(table, query, 0, null, null, null, null));
                continue;
            }
            Column column = findColumn(table, table.getPartitionColumn());
            if (!hasText(table.getPartitionStart()) || !hasText(table.getPartitionEnd())) {
                throw new IllegalArgumentException("partition_lower_bound and partition_upper_bound are required for partitioned table " + table.getTablePath());
            }
            List<? extends Chunk<?>> chunks = splitChunks(table, column, parallelism);
            for (int index = 0; index < chunks.size(); index++) {
                Chunk<?> chunk = chunks.get(index);
                String predicate = dialect.quoteIdentifier(column.getName()) + " >= " + literal(chunk.getStart())
                        + " AND " + dialect.quoteIdentifier(column.getName()) + (chunk.isEndInclusive() ? " <= " : " < ") + literal(chunk.getEnd());
                splits.add(split(table, appendPredicate(query, predicate), index, column.getName(), column.getDataType(), chunk.getStart(), chunk.getEnd()));
            }
        }

        return Collections.unmodifiableList(splits);
    }

    private static JdbcSourceSplit split(JdbcSourceTable table, String query, int index, String key, FluxDataType<?> type, Object start, Object end) {
        return new JdbcSourceSplit(table.getTablePath(), table.getTablePath() + "-" + index, query, key, type, start, end);
    }
    private static Column findColumn(JdbcSourceTable table, String name) {
        for (Column column : table.getCatalogTable().getTableSchema().getColumns()) if (column.getName().equalsIgnoreCase(name)) return column;
        throw new IllegalArgumentException("partition_column does not exist in source table: " + name + ", table=" + table.getTablePath());
    }
    private static List<? extends Chunk<?>> splitChunks(JdbcSourceTable table, Column column, int parallelism) {
        int requestedChunks = table.getPartitionNumber() == null ? parallelism : table.getPartitionNumber();
        if (String.class.equals(column.getDataType().getTypeClass())) {
            return new DynamicChunkSplitter<String>(new AsciiStringRangeSplitter(), parallelism)
                    .split(table.getPartitionStart(), table.getPartitionEnd(), requestedChunks);
        }
        try {
            return new DynamicChunkSplitter<BigDecimal>(new FixedChunkSplitter(), parallelism)
                    .split(
                            new BigDecimal(table.getPartitionStart()),
                            new BigDecimal(table.getPartitionEnd()),
                            requestedChunks);
        }
        catch (NumberFormatException e) { throw new IllegalArgumentException("partition bounds must be numeric for column " + column.getName(), e); }
    }
    private static String appendPredicate(String query, String predicate) { return "SELECT * FROM (" + query + ") AS flux_split WHERE " + predicate; }
    private static String literal(Object value) { return value instanceof Number || value instanceof BigDecimal ? value.toString() : "'" + value.toString().replace("'", "''") + "'"; }
    private static boolean hasText(String value) { return value != null && !value.trim().isEmpty(); }
}
