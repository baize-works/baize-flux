package com.baize.flux.api.source;

import java.io.Serializable;

/**
 * Flux 通用离线 Source。
 *
 * @param <T> 输出数据类型
 * @param <SplitT> 分片类型
 */
public interface Source<T, SplitT extends SourceSplit>
        extends Serializable {

    /**
     * 创建 Source 分片生成器。
     */
    SourceSplitEnumerator<SplitT> createSplitEnumerator(
            SourceEnumeratorContext context)
            throws Exception;

    /**
     * 创建 Source Reader。
     */
    SourceReader<T, SplitT> createReader(
            SourceReaderContext context)
            throws Exception;
}