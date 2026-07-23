package com.baize.flux.framework.execution.source;

import com.baize.flux.api.source.RecordBatch;

/**
 * Source 批次数据消费者。
 */
@FunctionalInterface
public interface RecordBatchConsumer<T> {

    void accept(RecordBatch<T> batch)
            throws Exception;
}