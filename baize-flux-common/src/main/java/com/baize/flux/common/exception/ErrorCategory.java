package com.baize.flux.common.exception;

/**
 * Baize Flux 错误分类。
 *
 * @author weifuwan
 */
public enum ErrorCategory {

    /**
     * 配置错误。
     */
    CONFIGURATION,

    /**
     * 插件错误。
     */
    PLUGIN,

    /**
     * 连接器错误。
     */
    CONNECTOR,

    /**
     * 数据处理错误。
     */
    DATA,

    /**
     * 任务执行错误。
     */
    EXECUTION,

    /**
     * 外部系统错误。
     */
    EXTERNAL,

    /**
     * 框架内部错误。
     */
    INTERNAL
}