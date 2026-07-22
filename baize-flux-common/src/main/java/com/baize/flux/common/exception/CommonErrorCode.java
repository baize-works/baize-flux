package com.baize.flux.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Baize Flux 公共错误码。
 *
 * @author weifuwan
 */
@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements FluxErrorCode {

    INVALID_ARGUMENT(
            "COMMON-001",
            "参数不合法",
            ErrorCategory.CONFIGURATION,
            false
    ),

    CONFIGURATION_INVALID(
            "COMMON-002",
            "配置内容不合法",
            ErrorCategory.CONFIGURATION,
            false
    );

    private final String code;

    private final String description;

    private final ErrorCategory category;

    private final boolean retryable;
}