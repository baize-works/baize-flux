package com.baize.flux.api.source;
import com.baize.flux.api.table.RecordBatch;
/** Reads one assigned split synchronously. */
public interface SourceReader<T, SplitT extends SourceSplit> extends AutoCloseable {
    void open(SplitT split) throws Exception;
    RecordBatch<T> pollBatch() throws Exception;
    boolean isFinished();
    @Override void close() throws Exception;
}
