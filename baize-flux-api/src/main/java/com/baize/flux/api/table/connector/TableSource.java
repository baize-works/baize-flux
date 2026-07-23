package com.baize.flux.api.table.connector;

import com.baize.flux.api.source.Source;
import com.baize.flux.api.source.SourceSplit;
import com.baize.flux.api.table.catalog.CatalogTable;
import com.baize.flux.api.table.type.FluxRow;

import java.util.List;

/**
 * 输出 FluxRow 的表类型 Source。
 *
 * JDBC、数据库查询、数据仓库等连接器可以实现该接口。
 */
public interface TableSource<SplitT extends SourceSplit>
        extends Source<FluxRow, SplitT> {

    /**
     * 返回当前 Source 输出的表结构。
     */
    List<CatalogTable> getProducedCatalogTables();
}