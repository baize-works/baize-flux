package com.baize.flux.api.configuration;

import com.baize.flux.common.exception.FluxException;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 配置访问异常。
 *
 * 当读取必要配置项，但未找到对应配置值时抛出。
 *
 * @author weifuwan
 */
public class ConfigAccessException extends FluxException {

    /**
     * 配置项名称。
     */
    private final String optionKey;

    /**
     * 根据异常明细创建配置访问异常。
     *
     * @param message 异常明细
     */
    public ConfigAccessException(String message) {
        this(null, message, null);
    }

    /**
     * 根据配置项名称和原始异常创建配置访问异常。
     *
     * @param optionKey 配置项名称
     * @param cause     原始异常
     */
    public ConfigAccessException(String optionKey, Throwable cause) {
        this(
                optionKey,
                String.format(
                        "Required configuration option '%s' is not configured.",
                        optionKey
                ),
                cause
        );
    }

    /**
     * 根据配置项名称和异常明细创建配置访问异常。
     *
     * @param optionKey 配置项名称
     * @param message   异常明细
     */
    public ConfigAccessException(String optionKey, String message) {
        this(optionKey, message, null);
    }

    /**
     * 根据配置项名称、异常明细和原始异常创建配置访问异常。
     *
     * @param optionKey 配置项名称
     * @param message   异常明细
     * @param cause     原始异常
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

    /**
     * 获取配置项名称。
     *
     * @return 配置项名称
     */
    public String optionKey() {
        return optionKey;
    }

    /**
     * 规范化异常明细。
     *
     * @param optionKey 配置项名称
     * @param message   异常明细
     * @return 规范化后的异常明细
     */
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

    /**
     * 构建异常上下文。
     *
     * @param optionKey 配置项名称
     * @return 异常上下文
     */
    private static Map<String, ?> buildContext(String optionKey) {
        if (optionKey == null || optionKey.trim().isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Object> context = new LinkedHashMap<>();
        context.put("optionKey", optionKey);
        return context;
    }
}