package com.baize.flux.api.sink;

import com.baize.flux.api.table.type.FluxRow;

/**
 * 不可变的 Sink 定义。
 *
 * <p>一个 Job 只创建一个 Sink；每个 SinkTask 必须通过 {@link #createWriter(SinkWriterContext)}
 * 创建自己的 Writer，不能在 Sink 中共享连接或可变写入状态。
 */
public interface Sink {

    /**
     * 为一个 SinkTask 创建线程封闭的 Writer。
     */
    SinkWriter<FluxRow> createWriter(SinkWriterContext context);
}
