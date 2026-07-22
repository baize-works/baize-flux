package com.baize.flux.api.configuration;

import com.baize.flux.common.exception.FluxException;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 读取必要配置项时，没有找到对应配置值。
 */
public class ConfigAccessException extends FluxException {

    private final String optionKey;

    /**
     * 兼容原有调用方式。
     *
     * @param message 异常明细
     */
    public ConfigAccessException(String message) {
        this(null, message, null);
    }

    /**
     * 根据配置项名称创建异常。
     *
     * @param optionKey 配置项名称
     */
    public ConfigAccessException(String optionKey, Throwable cause) {
        this(
                optionKey,
                String.format("Required configuration option '%s' is not configured.", optionKey),
                cause
        );
    }

    /**
     * 创建配置访问异常。
     *
     * @param optionKey 配置项名称
     * @param message 异常明细
     */
    public ConfigAccessException(String optionKey, String message) {
        this(optionKey, message, null);
    }

    /**
     * 创建配置访问异常。
     *
     * @param optionKey 配置项名称
     * @param message 异常明细
     * @param cause 原始异常
     */
    public ConfigAccessException(
            String optionKey,
            String message,
            Throwable cause) {
        super(
                ConfigurationErrorCode.CONFIG_VALUE_MISSING,
                normalizeMessage(optionKey, message),
                cause,
                buildContext(optionKey)
        );
        this.optionKey = optionKey;
    }

    public String optionKey() {
        return optionKey;
    }

    private static String normalizeMessage(
            String optionKey,
            String message) {
        if (message != null && !message.trim().isEmpty()) {
            return message;
        }

        if (optionKey != null && !optionKey.trim().isEmpty()) {
            return String.format(
                    "Required configuration option '%s' is not configured.",
                    optionKey
            );
        }

        return "Required configuration value is not available.";
    }

    private static Map<String, ?> buildContext(String optionKey) {
        if (optionKey == null || optionKey.trim().isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Object> context = new LinkedHashMap<String, Object>();
        context.put("optionKey", optionKey);
        return context;
    }
}