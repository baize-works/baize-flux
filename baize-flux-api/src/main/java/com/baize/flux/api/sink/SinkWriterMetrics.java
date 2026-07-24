package com.baize.flux.api.sink;

/** Sink Writer 可选使用的轻量指标入口。 */
public interface SinkWriterMetrics {
    void incrementWriteSuccessRecords(long count);
    void addWrittenBytes(long count);
}
