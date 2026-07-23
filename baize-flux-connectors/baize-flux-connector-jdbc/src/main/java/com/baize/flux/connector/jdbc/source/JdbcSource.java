package com.baize.flux.connector.jdbc.source;

import com.baize.flux.api.source.Source;
import com.baize.flux.api.source.SourceReader;
import com.baize.flux.api.table.catalog.CatalogTable;
import com.baize.flux.api.table.catalog.TablePath;
import com.baize.flux.api.table.type.FluxRow;
import com.baize.flux.connector.jdbc.config.JdbcSourceConfig;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * JDBC 离线 Source。
 * <p>
 * Source 本身只保存不可变配置，不直接持有 JDBC 连接。
 */
public final class JdbcSource
        implements Source<JdbcSourceSplit> {

    private static final long serialVersionUID = 1L;

    private final JdbcSourceConfig config;

    public JdbcSource(JdbcSourceConfig config) {
        this.config = Objects.requireNonNull(
                config,
                "config must not be null");
    }

    @Override
    public List<JdbcSourceSplit> createSplits(
            Map<TablePath, CatalogTable> tables)
            throws Exception {

        return createSplits(tables, 1);
    }

    @Override
    public List<JdbcSourceSplit> createSplits(
            Map<TablePath, CatalogTable> tables,
            int parallelism)
            throws Exception {

        /*
         * 这里替换成你现有的分片生成器。
         *
         * 例如：
         * return new JdbcSourceSplitEnumerator(config, tables)
         *         .enumerateSplits();
         */
        return JdbcSourceSplitGenerator.generate(
                config,
                tables,
                parallelism);
    }

    @Override
    public SourceReader<FluxRow, JdbcSourceSplit> createReader(
            Map<TablePath, CatalogTable> tables,
            int batchSize) {

        return new JdbcSourceReader(
                config,
                tables,
                batchSize);
    }
}
