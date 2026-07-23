package com.baize.flux.api.source;

import com.baize.flux.api.table.catalog.CatalogTable;
import com.baize.flux.api.table.catalog.TablePath;
import com.baize.flux.api.table.type.FluxRow;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 离线数据源。
 *
 * @param <SplitT> Source 分片类型
 */
public interface Source<SplitT extends SourceSplit>
        extends Serializable {

    /**
     * 根据表结构生成读取分片。
     */
    List<SplitT> createSplits(
            Map<TablePath, CatalogTable> tables)
            throws Exception;

    /**
     * 创建 SourceReader。
     *
     * 每个执行任务必须使用独立 Reader，
     * Reader 不应在多个线程之间共享。
     */
    SourceReader<FluxRow, SplitT> createReader(
            Map<TablePath, CatalogTable> tables,
            int batchSize);
}