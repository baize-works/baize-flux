package com.baize.flux.api.source;

import java.util.List;

/**
 * 离线 Source 数据读取器。
 *
 * @param <T>      输出数据类型
 * @param <SplitT> Source 分片类型
 */
public interface SourceReader<T, SplitT extends SourceSplit>
        extends AutoCloseable {

    /**
     * 打开 Reader，并设置当前 Reader 负责的分片。
     */
    default void open(List<SplitT> splits) throws Exception {
        throw new UnsupportedOperationException(
                "This SourceReader does not support opening a split list");
    }

    /** Opens task-scoped resources before individual splits are processed. */
    default void open() throws Exception {
        // Optional for legacy readers.
    }

    /** Opens resources for exactly one split. */
    default void openSplit(SplitT split) throws Exception {
        throw new UnsupportedOperationException(
                "This SourceReader does not support single-split reading");
    }

    /** Closes resources associated with the current split. */
    default void closeSplit() throws Exception {
        // Optional for legacy readers.
    }

    /**
     * 读取下一批数据。
     * <p>
     * 当全部分片读取完成时，返回 RecordBatch.endOfInput()。
     */
    RecordBatch<T> readBatch() throws Exception;

    @Override
    void close() throws Exception;
}
