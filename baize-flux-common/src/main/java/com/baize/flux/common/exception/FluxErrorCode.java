package com.baize.flux.common.exception;

/**
 * Baize Flux 统一错误码接口。
 *
 * @author weifuwan
 */
public interface FluxErrorCode {

    /**
     * Get error code
     *
     * @return error code
     */
    String getCode();

    /**
     * Get error description
     *
     * @return error description
     */
    String getDescription();

    default String getErrorMessage() {
        return String.format("ErrorCode:[%s], ErrorDescription:[%s]", getCode(), getDescription());
    }
}