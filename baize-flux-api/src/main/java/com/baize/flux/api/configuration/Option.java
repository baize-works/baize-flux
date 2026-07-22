package com.baize.flux.api.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 配置项元数据。
 *
 * 用于描述配置项名称、目标类型、类型转换器、默认值以及展示属性。
 * 配置项是否必填由 {@link OptionRule} 定义，不属于当前类的职责。
 *
 * @param <T> 配置值类型
 * @author weifuwan
 */
public final class Option<T> {

    /**
     * 配置项名称。
     */
    private final String key;

    /**
     * 配置值目标类型名称。
     */
    private final String typeName;

    /**
     * 配置值转换器。
     */
    private final ConfigConverter<T> converter;

    /**
     * 配置项默认值。
     */
    private final T defaultValue;

    /**
     * 是否声明了默认值。
     */
    private final boolean hasDefaultValue;

    /**
     * 配置项描述。
     */
    private final String description;

    /**
     * 兼容的备用配置项名称。
     */
    private final List<String> fallbackKeys;

    /**
     * 配置项允许使用的值。
     */
    private final List<T> allowedValues;

    /**
     * 配置项是否包含敏感信息。
     */
    private final boolean sensitive;

    /**
     * 是否允许包含任意嵌套配置项。
     */
    private final boolean allowNestedKeys;

    /**
     * 创建配置项元数据。
     *
     * @param key             配置项名称
     * @param typeName        配置值目标类型名称
     * @param converter       配置值转换器
     * @param defaultValue    配置项默认值
     * @param hasDefaultValue 是否声明了默认值
     * @param description     配置项描述
     * @param fallbackKeys    备用配置项名称
     * @param allowedValues   允许使用的配置值
     * @param sensitive       是否包含敏感信息
     * @param allowNestedKeys 是否允许任意嵌套配置项
     */
    Option(
            String key,
            String typeName,
            ConfigConverter<T> converter,
            T defaultValue,
            boolean hasDefaultValue,
            String description,
            List<String> fallbackKeys,
            List<T> allowedValues,
            boolean sensitive,
            boolean allowNestedKeys) {
        this.key =
                requireText(
                        key,
                        "Option key must not be blank"
                );
        this.typeName =
                requireText(
                        typeName,
                        "Option type name must not be blank"
                );
        this.converter =
                Objects.requireNonNull(
                        converter,
                        "converter"
                );

        if (hasDefaultValue && defaultValue == null) {
            throw new IllegalArgumentException(
                    "Default value must not be null"
            );
        }

        this.defaultValue = defaultValue;
        this.hasDefaultValue = hasDefaultValue;
        this.description =
                description == null
                        ? ""
                        : description;
        this.fallbackKeys =
                Collections.unmodifiableList(
                        new ArrayList<>(fallbackKeys)
                );
        this.allowedValues =
                Collections.unmodifiableList(
                        new ArrayList<>(allowedValues)
                );
        this.sensitive = sensitive;
        this.allowNestedKeys = allowNestedKeys;

        if (hasDefaultValue
                && !this.allowedValues.isEmpty()
                && !this.allowedValues.contains(defaultValue)) {
            throw new IllegalArgumentException(
                    "Default value of option '"
                            + key
                            + "' must be one of "
                            + this.allowedValues
            );
        }

        if (this.fallbackKeys.contains(key)) {
            throw new IllegalArgumentException(
                    "Fallback keys of option '"
                            + key
                            + "' must not contain itself"
            );
        }
    }

    /**
     * 获取配置项名称。
     *
     * @return 配置项名称
     */
    public String key() {
        return key;
    }

    /**
     * 获取配置值目标类型名称。
     *
     * @return 配置值目标类型名称
     */
    public String typeName() {
        return typeName;
    }

    /**
     * 获取配置值转换器。
     *
     * @return 配置值转换器
     */
    ConfigConverter<T> converter() {
        return converter;
    }

    /**
     * 判断配置项是否声明了默认值。
     *
     * @return 声明了默认值时返回 {@code true}
     */
    public boolean hasDefaultValue() {
        return hasDefaultValue;
    }

    /**
     * 获取配置项默认值。
     *
     * @return 配置项默认值
     */
    public T defaultValue() {
        return defaultValue;
    }

    /**
     * 获取可选形式的配置项默认值。
     *
     * @return 配置项默认值
     */
    public Optional<T> defaultValueOptional() {
        return hasDefaultValue
                ? Optional.ofNullable(defaultValue)
                : Optional.empty();
    }

    /**
     * 获取配置项描述。
     *
     * @return 配置项描述
     */
    public String description() {
        return description;
    }

    /**
     * 获取备用配置项名称。
     *
     * @return 不可修改的备用配置项名称集合
     */
    public List<String> fallbackKeys() {
        return fallbackKeys;
    }

    /**
     * 获取配置项允许使用的值。
     *
     * @return 不可修改的允许值集合
     */
    public List<T> allowedValues() {
        return allowedValues;
    }

    /**
     * 判断配置项是否包含敏感信息。
     *
     * @return 包含敏感信息时返回 {@code true}
     */
    public boolean sensitive() {
        return sensitive;
    }

    /**
     * 判断是否允许配置项包含任意子配置项。
     *
     * 启用后，未知配置项校验不会拒绝当前配置项下未明确声明的子配置项。
     *
     * @return 允许任意子配置项时返回 {@code true}
     */
    public boolean allowNestedKeys() {
        return allowNestedKeys;
    }

    /**
     * 将原始配置值转换为配置项声明的目标类型。
     *
     * @param rawValue 原始配置值
     * @return 转换后的配置值
     * @throws ConfigConversionException 配置值转换失败时抛出
     */
    T convert(Object rawValue) {
        try {
            return converter.convert(rawValue);
        } catch (ConfigConversionException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ConfigConversionException(
                    key,
                    rawValue,
                    typeName,
                    "Unable to convert option '"
                            + key
                            + "' to "
                            + typeName,
                    e
            );
        }
    }

    /**
     * 返回配置项的字符串表示。
     *
     * 字符串中仅包含配置项名称和类型，不包含配置值，
     * 避免敏感配置内容泄露。
     *
     * @return 配置项字符串表示
     */
    @Override
    public String toString() {
        return "Option{"
                + "key='"
                + key
                + '\''
                + ", type='"
                + typeName
                + '\''
                + '}';
    }

    /**
     * 校验文本内容不为空。
     *
     * @param value   待校验文本
     * @param message 校验失败时的异常信息
     * @return 原始文本
     */
    private static String requireText(
            String value,
            String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }

        return value;
    }
}
