package com.baize.flux.connector.jdbc.source;

import com.baize.flux.api.source.SourceEnumeratorContext;
import com.baize.flux.api.source.SourceReader;
import com.baize.flux.api.source.SourceReaderContext;
import com.baize.flux.api.source.SourceSplitEnumerator;
import com.baize.flux.api.table.catalog.CatalogTable;
import com.baize.flux.api.table.catalog.TablePath;
import com.baize.flux.api.table.connector.TableSource;
import com.baize.flux.api.table.type.FluxRow;
import com.baize.flux.connector.jdbc.catalog.JdbcCatalogUtils;
import com.baize.flux.connector.jdbc.config.JdbcSourceConfig;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * JDBC 离线 Source。
 *
 * 只负责：
 * 1. 保存 JDBC 配置；
 * 2. 保存读取表信息；
 * 3. 创建分片生成器；
 * 4. 创建数据 Reader。
 */
public final class JdbcSource
        implements TableSource<JdbcSourceSplit> {

    private static final long serialVersionUID = 1L;

    private final JdbcSourceConfig config;

    private final Map<TablePath, JdbcSourceTable> sourceTables;

    public JdbcSource(JdbcSourceConfig config)
            throws Exception {

        this.config = config;

        this.sourceTables =
                JdbcCatalogUtils.getTables(
                        config.getJdbcConnectionConfig(),
                        config.getTableConfigList(),
                        config.getMultiTableFailurePolicy());
    }

    @Override
    public List<CatalogTable> getProducedCatalogTables() {
        return sourceTables.values()
                .stream()
                .map(JdbcSourceTable::getCatalogTable)
                .collect(Collectors.toList());
    }

    @Override
    public SourceSplitEnumerator<JdbcSourceSplit>
    createSplitEnumerator(
            SourceEnumeratorContext context) {

        return new JdbcSourceSplitEnumerator(
                config,
                sourceTables,
                context.getParallelism());
    }

    @Override
    public SourceReader<FluxRow, JdbcSourceSplit>
    createReader(
            SourceReaderContext context) {

        Map<TablePath, CatalogTable> catalogTables =
                sourceTables.entrySet()
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        Map.Entry::getKey,
                                        entry ->
                                                entry.getValue()
                                                        .getCatalogTable()));

        return new JdbcSourceReader(
                config,
                catalogTables,
                context.getBatchSize());
    }
}
