package com.baize.flux.api.source;

import com.baize.flux.api.factory.Factory;

/**
 * 通用 Source 工厂。
 *
 * @param <T> Source 输出数据类型
 * @param <SplitT> Source 分片类型
 */
public interface SourceFactory<T, SplitT extends SourceSplit>
        extends Factory {

    /**
     * 根据配置创建 Source。
     */
    Source<T, SplitT> createSource(
            SourceFactoryContext context)
            throws Exception;
}