package com.baize.flux.api.sink;

import com.baize.flux.api.source.RecordBatch;
import com.baize.flux.api.table.catalog.CatalogTable;

/**
 * 一个 SinkTask 对应一个 SinkWriter。
 *
 * <p>事务生命周期：
 *
 * <pre>
 * open
 *   -> write
 *   -> write
 *   -> commit / rollback
 *   -> close
 * </pre>
 */
public interface SinkWriter<T> extends AutoCloseable {

    /**
     * 初始化 Writer。
     *
     * <p>事务型 Sink 可在这里初始化事务上下文；
     * 非事务型 Sink 可以使用默认实现。
     */
    default void open() throws Exception {
    }

    /**
     * 写入一批数据。
     *
     * <p>调用 write 成功不代表数据已经最终提交。
     */
    void write(
            RecordBatch<T> batch,
            CatalogTable sourceTable)
            throws Exception;

    /**
     * 当前 SinkTask 全部数据写入成功后提交。
     */
    default void commit() throws Exception {
    }

    /**
     * 当前 SinkTask 执行失败时回滚。
     */
    default void rollback() throws Exception {
    }

    /**
     * 释放 Writer 持有的资源。
     *
     * <p>close 不负责提交事务。
     */
    @Override
    void close() throws Exception;
}