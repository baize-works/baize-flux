package com.baize.flux.api.configuration;

import com.baize.flux.common.exception.FluxException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 原始配置值无法转换成配置项声明的目标类型。
 *
 * <p>异常中不保存原始配置值，避免密码、Token 等敏感信息被异常对象或日志泄露。</p>
 */
public class ConfigConversionException extends FluxException {

    private final String optionKey;
    private final String expectedType;
    private final String rawValueType;

    /**
     * 推荐使用的构造方法。
     *
     * @param optionKey 配置项名称
     * @param expectedType 期望类型
     * @param cause 原始异常
     */
    public ConfigConversionException(
            String optionKey,
            String expectedType,
            Throwable cause) {
        this(
                optionKey,
                expectedType,
                null,
                cause
        );
    }

    /**
     * 兼容原有调用方式。
     *
     * <p>rawValue 仅用于获取类型，不会保存，也不会写入异常信息。</p>
     *
     * @param optionKey 配置项名称
     * @param rawValue 原始配置值
     * @param expectedType 期望类型
     * @param message 原异常信息，为避免敏感数据泄露，不直接输出
     * @param cause 原始异常
     */
    public ConfigConversionException(
            String optionKey,
            Object rawValue,
            String expectedType,
            String message,
            Throwable cause) {
        this(
                optionKey,
                expectedType,
                resolveRawValueType(rawValue),
                cause
        );
    }

    private ConfigConversionException(
            String optionKey,
            String expectedType,
            String rawValueType,
            Throwable cause) {
        super(
                ConfigurationErrorCode.CONFIG_VALUE_CONVERSION_FAILED,
                buildMessage(optionKey, expectedType, rawValueType),
                cause,
                buildContext(optionKey, expectedType, rawValueType)
        );
        this.optionKey = optionKey;
        this.expectedType = expectedType;
        this.rawValueType = rawValueType;
    }

    public String optionKey() {
        return optionKey;
    }

    public String expectedType() {
        return expectedType;
    }

    public String rawValueType() {
        return rawValueType;
    }

    private static String buildMessage(
            String optionKey,
            String expectedType,
            String rawValueType) {
        String normalizedKey =
                optionKey == null || optionKey.trim().isEmpty()
                        ? "<unknown>"
                        : optionKey;

        String normalizedExpectedType =
                expectedType == null || expectedType.trim().isEmpty()
                        ? "<unknown>"
                        : expectedType;

        if (rawValueType == null || rawValueType.trim().isEmpty()) {
            return String.format(
                    "Configuration option '%s' cannot be converted to type '%s'.",
                    normalizedKey,
                    normalizedExpectedType
            );
        }

        return String.format(
                "Configuration option '%s' with raw type '%s' cannot be converted to type '%s'.",
                normalizedKey,
                rawValueType,
                normalizedExpectedType
        );
    }

    private static Map<String, ?> buildContext(
            String optionKey,
            String expectedType,
            String rawValueType) {
        Map<String, Object> context = new LinkedHashMap<>();

        if (optionKey != null && !optionKey.trim().isEmpty()) {
            context.put("optionKey", optionKey);
        }

        if (expectedType != null && !expectedType.trim().isEmpty()) {
            context.put("expectedType", expectedType);
        }

        if (rawValueType != null && !rawValueType.trim().isEmpty()) {
            context.put("rawValueType", rawValueType);
        }

        return context;
    }

    private static String resolveRawValueType(Object rawValue) {
        return rawValue == null
                ? "null"
                : rawValue.getClass().getTypeName();
    }
}