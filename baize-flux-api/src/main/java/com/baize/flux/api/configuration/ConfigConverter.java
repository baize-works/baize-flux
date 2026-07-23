package com.baize.flux.api.configuration;

/**
 * 配置值转换器。
 * <p>
 * 用于将原始配置值转换为 {@link Option} 声明的目标类型。
 *
 * @param <T> 目标配置值类型
 * @author weifuwan
 */
@FunctionalInterface
public interface ConfigConverter<T> {

    /**
     * 将原始配置值转换为目标类型。
     *
     * @param rawValue 原始配置值
     * @return 转换后的配置值
     */
    T convert(Object rawValue);
}
