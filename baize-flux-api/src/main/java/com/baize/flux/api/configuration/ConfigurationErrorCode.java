package com.baize.flux.api.configuration;

import com.baize.flux.common.exception.ErrorCategory;
import com.baize.flux.common.exception.FluxErrorCode;

/**
 * Baize Flux 配置体系错误码。
 */
public enum ConfigurationErrorCode implements FluxErrorCode {

    CONFIG_VALUE_MISSING(
            "CONFIG-001",
            "缺少必要的配置项",
            ErrorCategory.CONFIGURATION,
            false
    ),

    CONFIG_VALUE_CONVERSION_FAILED(
            "CONFIG-002",
            "配置值类型转换失败",
            ErrorCategory.CONFIGURATION,
            false
    ),

    CONFIG_VALIDATION_FAILED(
            "CONFIG-003",
            "配置校验失败",
            ErrorCategory.CONFIGURATION,
            false
    ),

    CONFIG_PARSE_FAILED(
            "CONFIG-004",
            "配置解析失败",
            ErrorCategory.CONFIGURATION,
            false
    );

    private final String code;
    private final String description;
    private final ErrorCategory category;
    private final boolean retryable;

    ConfigurationErrorCode(
            String code,
            String description,
            ErrorCategory category,
            boolean retryable) {
        this.code = code;
        this.description = description;
        this.category = category;
        this.retryable = retryable;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public ErrorCategory getCategory() {
        return category;
    }

    @Override
    public boolean isRetryable() {
        return retryable;
    }
}