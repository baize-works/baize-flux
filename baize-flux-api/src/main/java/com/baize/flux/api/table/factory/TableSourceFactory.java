package com.baize.flux.api.table.factory;

import com.baize.flux.api.source.SourceFactory;
import com.baize.flux.api.source.SourceFactoryContext;
import com.baize.flux.api.source.SourceSplit;
import com.baize.flux.api.table.catalog.CatalogTable;
import com.baize.flux.api.table.connector.TableSource;
import com.baize.flux.api.table.type.FluxRow;

import java.util.List;

/**
 * 表类型 Source 工厂。
 */
public interface TableSourceFactory<SplitT extends SourceSplit>
        extends SourceFactory<FluxRow, SplitT> {

    @Override
    TableSource<SplitT> createSource(
            SourceFactoryContext context)
            throws Exception;

    /**
     * 读取数据源元数据并发现表结构。
     *
     * 该方法可以用于：
     * 1. dry-run；
     * 2. Web 页面预览字段；
     * 3. Source 和 Sink 字段映射；
     * 4. 提交任务前校验表是否存在。
     */
    List<CatalogTable> discoverTableSchemas(
            SourceFactoryContext context)
            throws Exception;
}