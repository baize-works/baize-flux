package com.baize.flux.api.table.factory;

import com.baize.flux.api.factory.SourceFactory;
import com.baize.flux.api.source.Source;
import com.baize.flux.api.source.SourceFactoryContext;
import com.baize.flux.api.source.SourceSplit;
import com.baize.flux.api.table.catalog.CatalogTable;

import java.util.List;

/**
 * 支持表结构发现的 Source 工厂。
 *
 * @param <SplitT> Source 分片类型
 */
public interface TableSourceFactory<SplitT extends SourceSplit>
        extends SourceFactory {

    /**
     * 创建 Source。
     */
    Source<SplitT> createSource(
            SourceFactoryContext context)
            throws Exception;

    /**
     * 发现 Source 输出表结构。
     */
    List<CatalogTable> discoverTableSchemas(
            SourceFactoryContext context)
            throws Exception;
}