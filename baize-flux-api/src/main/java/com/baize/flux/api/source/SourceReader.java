package com.baize.flux.api.source;

import java.util.List;

/**
 * 离线 Source 数据读取器。
 *
 * @param <T> 输出数据类型
 * @param <SplitT> Source 分片类型
 */
public interface SourceReader<T, SplitT extends SourceSplit>
        extends AutoCloseable {

    /**
     * 打开 Reader，并设置当前 Reader 负责的分片。
     */
    void open(List<SplitT> splits) throws Exception;

    /**
     * 读取下一批数据。
     *
     * 当全部分片读取完成时，返回 RecordBatch.endOfInput()。
     */
    RecordBatch<T> readBatch() throws Exception;

    @Override
    void close() throws Exception;
}