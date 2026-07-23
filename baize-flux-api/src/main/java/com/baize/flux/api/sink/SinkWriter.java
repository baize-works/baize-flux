package com.baize.flux.api.sink;
import com.baize.flux.api.table.RecordBatch;
/** Transactional destination writer for one local job. */
public interface SinkWriter<T> extends AutoCloseable {
    void open() throws Exception;
    void write(RecordBatch<T> batch) throws Exception;
    void flush() throws Exception;
    void commit() throws Exception;
    void rollback() throws Exception;
    @Override void close() throws Exception;
}
