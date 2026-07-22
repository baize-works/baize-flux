package com.baize.flux.api.configuration;

import com.baize.flux.common.exception.FluxException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 配置值转换异常。
 *
 * 当原始配置值无法转换为配置项声明的目标类型时抛出。
 * 异常中不保存原始配置值，避免密码、Token 等敏感信息泄露。
 *
 * @author weifuwan
 */
public class ConfigConversionException extends FluxException {

    /**
     * 配置项名称。
     */
    private final String optionKey;

    /**
     * 期望转换的目标类型。
     */
    private final String expectedType;

    /**
     * 原始配置值的类型。
     */
    private final String rawValueType;

    /**
     * 根据配置项名称、目标类型和原始异常创建配置值转换异常。
     *
     * @param optionKey    配置项名称
     * @param expectedType 目标类型
     * @param cause        原始异常
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
     * 根据配置项名称、原始配置值、目标类型和原始异常创建配置值转换异常。
     *
     * 原始配置值仅用于获取类型，不会保存到异常对象或写入异常信息。
     *
     * @param optionKey    配置项名称
     * @param rawValue     原始配置值
     * @param expectedType 目标类型
     * @param message      原异常信息，不会直接写入异常信息
     * @param cause        原始异常
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

    /**
     * 根据配置项名称、目标类型、原始值类型和原始异常创建配置值转换异常。
     *
     * @param optionKey    配置项名称
     * @param expectedType 目标类型
     * @param rawValueType 原始配置值类型
     * @param cause        原始异常
     */
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

    /**
     * 获取配置项名称。
     *
     * @return 配置项名称
     */
    public String optionKey() {
        return optionKey;
    }

    /**
     * 获取期望转换的目标类型。
     *
     * @return 目标类型
     */
    public String expectedType() {
        return expectedType;
    }

    /**
     * 获取原始配置值的类型。
     *
     * @return 原始配置值类型
     */
    public String rawValueType() {
        return rawValueType;
    }

    /**
     * 构建异常明细。
     *
     * @param optionKey    配置项名称
     * @param expectedType 目标类型
     * @param rawValueType 原始配置值类型
     * @return 异常明细
     */
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

    /**
     * 构建异常上下文。
     *
     * @param optionKey    配置项名称
     * @param expectedType 目标类型
     * @param rawValueType 原始配置值类型
     * @return 异常上下文
     */
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

    /**
     * 获取原始配置值的类型名称。
     *
     * @param rawValue 原始配置值
     * @return 原始配置值类型名称
     */
    private static String resolveRawValueType(Object rawValue) {
        return rawValue == null
                ? "null"
                : rawValue.getClass().getTypeName();
    }
}