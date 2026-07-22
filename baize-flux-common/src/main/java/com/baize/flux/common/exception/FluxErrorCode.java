package com.baize.flux.common.exception;

/**
 * Baize Flux 统一错误码接口。
 *
 * @author weifuwan
 */
public interface FluxErrorCode {

    /**
     * 错误码，例如：COMMON-001、CORE-001。
     */
    String getCode();

    /**
     * 错误描述。
     */
    String getDescription();

    /**
     * 错误分类。
     */
    ErrorCategory getCategory();

    /**
     * 是否建议重试。
     */
    default boolean isRetryable() {
        return false;
    }

    /**
     * 格式化错误信息。
     */
    default String format() {
        return String.format(
                "ErrorCode:[%s], Category:[%s], Description:[%s]",
                getCode(),
                getCategory(),
                getDescription()
        );
    }
}