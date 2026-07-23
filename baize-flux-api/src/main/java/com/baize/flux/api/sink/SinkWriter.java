package com.baize.flux.api.sink;

/**
 * Transactional destination writer for one local job.
 */
public interface SinkWriter<T> extends AutoCloseable {

    @Override
    void close() throws Exception;
}
