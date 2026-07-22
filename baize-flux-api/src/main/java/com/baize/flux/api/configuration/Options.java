package com.baize.flux.api.configuration;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * 配置项工厂类。
 *
 * 用于根据配置项名称和目标类型，通过链式构建方式创建 {@link Option}。
 *
 * @author weifuwan
 */
public final class Options {

    /**
     * 工具类不允许实例化。
     */
    private Options() {
    }

    /**
     * 根据配置项名称创建配置项构建器。
     *
     * @param key 配置项名称
     * @return 配置项构建器
     */
    public static OptionBuilder key(String key) {
        if (isBlank(key)) {
            throw new IllegalArgumentException(
                    "Option key must not be blank"
            );
        }

        return new OptionBuilder(key);
    }

    /**
     * 配置项类型构建器。
     *
     * 用于为指定配置项选择目标类型和对应的类型转换器。
     */
    public static final class OptionBuilder {

        /**
         * 配置项名称。
         */
        private final String key;

        /**
         * 创建配置项类型构建器。
         *
         * @param key 配置项名称
         */
        private OptionBuilder(String key) {
            this.key = key;
        }

        /**
         * 将配置项声明为字符串类型。
         *
         * @return 字符串类型配置项构建器
         */
        public TypedOptionBuilder<String> stringType() {
            return typed(
                    "String",
                    new ConfigConverter<String>() {
                        @Override
                        public String convert(Object raw) {
                            return toStringValue(raw);
                        }
                    },
                    false
            );
        }

        /**
         * 将配置项声明为整数类型。
         *
         * @return 整数类型配置项构建器
         */
        public TypedOptionBuilder<Integer> intType() {
            return typed(
                    "Integer",
                    new ConfigConverter<Integer>() {
                        @Override
                        public Integer convert(Object raw) {
                            return toInteger(raw);
                        }
                    },
                    false
            );
        }

        /**
         * 将配置项声明为长整数类型。
         *
         * @return 长整数类型配置项构建器
         */
        public TypedOptionBuilder<Long> longType() {
            return typed(
                    "Long",
                    new ConfigConverter<Long>() {
                        @Override
                        public Long convert(Object raw) {
                            return toLong(raw);
                        }
                    },
                    false
            );
        }

        /**
         * 将配置项声明为单精度浮点数类型。
         *
         * @return 单精度浮点数类型配置项构建器
         */
        public TypedOptionBuilder<Float> floatType() {
            return typed(
                    "Float",
                    new ConfigConverter<Float>() {
                        @Override
                        public Float convert(Object raw) {
                            return toFloat(raw);
                        }
                    },
                    false
            );
        }

        /**
         * 将配置项声明为双精度浮点数类型。
         *
         * @return 双精度浮点数类型配置项构建器
         */
        public TypedOptionBuilder<Double> doubleType() {
            return typed(
                    "Double",
                    new ConfigConverter<Double>() {
                        @Override
                        public Double convert(Object raw) {
                            return toDouble(raw);
                        }
                    },
                    false
            );
        }

        /**
         * 将配置项声明为高精度小数类型。
         *
         * @return 高精度小数类型配置项构建器
         */
        public TypedOptionBuilder<BigDecimal> bigDecimalType() {
            return typed(
                    "BigDecimal",
                    new ConfigConverter<BigDecimal>() {
                        @Override
                        public BigDecimal convert(Object raw) {
                            return toBigDecimal(raw);
                        }
                    },
                    false
            );
        }

        /**
         * 将配置项声明为布尔类型。
         *
         * @return 布尔类型配置项构建器
         */
        public TypedOptionBuilder<Boolean> booleanType() {
            return typed(
                    "Boolean",
                    new ConfigConverter<Boolean>() {
                        @Override
                        public Boolean convert(Object raw) {
                            return toBoolean(raw);
                        }
                    },
                    false
            );
        }

        /**
         * 将配置项声明为时间间隔类型。
         *
         * @return 时间间隔类型配置项构建器
         */
        public TypedOptionBuilder<Duration> durationType() {
            return typed(
                    "Duration",
                    new ConfigConverter<Duration>() {
                        @Override
                        public Duration convert(Object raw) {
                            return toDuration(raw);
                        }
                    },
                    false
            );
        }

        /**
         * 将配置项声明为指定枚举类型。
         *
         * @param enumType 枚举类型
         * @param <E>      枚举值类型
         * @return 枚举类型配置项构建器
         */
        public <E extends Enum<E>> TypedOptionBuilder<E> enumType(
                final Class<E> enumType) {

            Objects.requireNonNull(
                    enumType,
                    "enumType must not be null"
            );

            return typed(
                    enumType.getSimpleName(),
                    new ConfigConverter<E>() {
                        @Override
                        public E convert(Object raw) {
                            return toEnum(raw, enumType);
                        }
                    },
                    false
            );
        }

        /**
         * 将配置项声明为字符串列表类型。
         *
         * @return 字符串列表类型配置项构建器
         */
        public TypedOptionBuilder<List<String>> listType() {
            return typed(
                    "List<String>",
                    new ConfigConverter<List<String>>() {
                        @Override
                        public List<String> convert(Object raw) {
                            return toList(
                                    raw,
                                    new Function<Object, String>() {
                                        @Override
                                        public String apply(Object value) {
                                            return toStringValue(value);
                                        }
                                    }
                            );
                        }
                    },
                    false
            );
        }

        /**
         * 将配置项声明为指定元素类型的列表。
         *
         * @param elementType      列表元素类型
         * @param elementConverter 列表元素转换器
         * @param <T>              列表元素类型
         * @return 列表类型配置项构建器
         */
        public <T> TypedOptionBuilder<List<T>> listType(
                final Class<T> elementType,
                final ConfigConverter<T> elementConverter) {

            Objects.requireNonNull(
                    elementType,
                    "elementType must not be null"
            );
            Objects.requireNonNull(
                    elementConverter,
                    "elementConverter must not be null"
            );

            return typed(
                    "List<" + elementType.getSimpleName() + ">",
                    new ConfigConverter<List<T>>() {
                        @Override
                        public List<T> convert(Object raw) {
                            return toList(
                                    raw,
                                    new Function<Object, T>() {
                                        @Override
                                        public T apply(Object value) {
                                            return elementConverter.convert(
                                                    value
                                            );
                                        }
                                    }
                            );
                        }
                    },
                    false
            );
        }

        /**
         * 将配置项声明为字符串键值映射类型。
         *
         * 默认允许包含任意嵌套配置项。
         *
         * @return 字符串键值映射类型配置项构建器
         */
        public TypedOptionBuilder<Map<String, String>> mapType() {
            return typed(
                    "Map<String,String>",
                    new ConfigConverter<Map<String, String>>() {
                        @Override
                        public Map<String, String> convert(Object raw) {
                            return toStringMap(raw);
                        }
                    },
                    true
            );
        }

        /**
         * 将配置项声明为对象键值映射类型。
         *
         * 默认允许包含任意嵌套配置项。
         *
         * @return 对象键值映射类型配置项构建器
         */
        public TypedOptionBuilder<Map<String, Object>> mapObjectType() {
            return typed(
                    "Map<String,Object>",
                    new ConfigConverter<Map<String, Object>>() {
                        @Override
                        public Map<String, Object> convert(Object raw) {
                            return toObjectMap(raw);
                        }
                    },
                    true
            );
        }

        /**
         * 将配置项声明为自定义类型。
         *
         * 自定义类型由业务模块或连接器提供转换器，避免配置核心模块
         * 依赖 JSON 映射器或反射框架。
         *
         * @param typeName 配置值目标类型名称
         * @param converter 配置值转换器
         * @param <T> 配置值类型
         * @return 自定义类型配置项构建器
         */
        public <T> TypedOptionBuilder<T> customType(
                String typeName,
                ConfigConverter<T> converter) {

            if (isBlank(typeName)) {
                throw new IllegalArgumentException(
                        "typeName must not be blank"
                );
            }

            Objects.requireNonNull(
                    converter,
                    "converter must not be null"
            );

            return typed(
                    typeName,
                    converter,
                    false
            );
        }

        /**
         * 创建指定类型的配置项构建器。
         *
         * @param typeName        配置值目标类型名称
         * @param converter       配置值转换器
         * @param allowNestedKeys 是否允许任意嵌套配置项
         * @param <T>             配置值类型
         * @return 指定类型的配置项构建器
         */
        private <T> TypedOptionBuilder<T> typed(
                String typeName,
                ConfigConverter<T> converter,
                boolean allowNestedKeys) {

            return new TypedOptionBuilder<>(
                    key,
                    typeName,
                    converter,
                    allowNestedKeys
            );
        }
    }

    /**
     * 指定类型的配置项构建器。
     *
     * 用于设置配置项描述、备用名称、允许值、敏感属性和默认值。
     *
     * @param <T> 配置值类型
     */
    public static final class TypedOptionBuilder<T> {

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
         * 配置项描述。
         */
        private String description = "";

        /**
         * 备用配置项名称。
         */
        private final List<String> fallbackKeys =
                new ArrayList<>();

        /**
         * 配置项允许使用的值。
         */
        private final List<T> allowedValues =
                new ArrayList<>();

        /**
         * 配置项是否包含敏感信息。
         */
        private boolean sensitive;

        /**
         * 是否允许包含任意嵌套配置项。
         */
        private boolean allowNestedKeys;

        /**
         * 创建指定类型的配置项构建器。
         *
         * @param key             配置项名称
         * @param typeName        配置值目标类型名称
         * @param converter       配置值转换器
         * @param allowNestedKeys 是否允许任意嵌套配置项
         */
        private TypedOptionBuilder(
                String key,
                String typeName,
                ConfigConverter<T> converter,
                boolean allowNestedKeys) {

            this.key = key;
            this.typeName = typeName;
            this.converter = converter;
            this.allowNestedKeys = allowNestedKeys;
        }

        /**
         * 设置配置项描述。
         *
         * @param description 配置项描述
         * @return 当前构建器
         */
        public TypedOptionBuilder<T> description(
                String description) {

            this.description =
                    description == null
                            ? ""
                            : description;

            return this;
        }

        /**
         * 添加备用配置项名称。
         *
         * 空白的备用配置项名称将被忽略。
         *
         * @param fallbackKeys 备用配置项名称
         * @return 当前构建器
         */
        public TypedOptionBuilder<T> fallbackKeys(
                String... fallbackKeys) {

            if (fallbackKeys == null) {
                return this;
            }

            for (String fallbackKey : fallbackKeys) {
                if (!isBlank(fallbackKey)) {
                    this.fallbackKeys.add(
                            fallbackKey.trim()
                    );
                }
            }

            return this;
        }

        /**
         * 添加配置项允许使用的值。
         *
         * @param values 允许使用的配置值
         * @return 当前构建器
         */
        @SafeVarargs
        public final TypedOptionBuilder<T> allowedValues(
                T... values) {

            if (values != null) {
                this.allowedValues.addAll(
                        Arrays.asList(values)
                );
            }

            return this;
        }

        /**
         * 添加配置项允许使用的值。
         *
         * @param values 允许使用的配置值集合
         * @return 当前构建器
         */
        public TypedOptionBuilder<T> allowedValues(
                Collection<T> values) {

            if (values != null) {
                this.allowedValues.addAll(values);
            }

            return this;
        }

        /**
         * 将配置项标记为敏感配置项。
         *
         * @return 当前构建器
         */
        public TypedOptionBuilder<T> sensitive() {
            this.sensitive = true;
            return this;
        }

        /**
         * 允许配置项包含任意子配置项。
         *
         * 适用于 properties 等动态键值形式的 Map 配置项。
         *
         * @return 当前构建器
         */
        public TypedOptionBuilder<T> allowNestedKeys() {
            this.allowNestedKeys = true;
            return this;
        }

        /**
         * 设置配置项默认值并构建配置项。
         *
         * @param value 配置项默认值
         * @return 配置项
         */
        public Option<T> defaultValue(T value) {
            if (value == null) {
                throw new IllegalArgumentException(
                        "Default value must not be null"
                );
            }

            return build(value, true);
        }

        /**
         * 构建不包含默认值的配置项。
         *
         * @return 配置项
         */
        public Option<T> noDefaultValue() {
            return build(null, false);
        }

        /**
         * 构建配置项。
         *
         * @param defaultValue    配置项默认值
         * @param hasDefaultValue 是否声明了默认值
         * @return 配置项
         */
        private Option<T> build(
                T defaultValue,
                boolean hasDefaultValue) {

            return new Option<>(
                    key,
                    typeName,
                    converter,
                    defaultValue,
                    hasDefaultValue,
                    description,
                    new ArrayList<>(fallbackKeys),
                    new ArrayList<>(allowedValues),
                    sensitive,
                    allowNestedKeys
            );
        }
    }

    /**
     * 将原始配置值转换为字符串。
     *
     * 仅接受字符串类型的原始配置值，不会通过 {@code toString()}
     * 隐式转换其他类型。
     *
     * @param raw 原始配置值
     * @return 字符串配置值
     */
    private static String toStringValue(Object raw) {
        requireRawValue(raw);

        if (!(raw instanceof String)) {
            throw new IllegalArgumentException(
                    "Expected String value, but got "
                            + raw.getClass().getName()
            );
        }

        return (String) raw;
    }

    /**
     * 将数字精确转换为长整数。
     *
     * 包含小数部分或超出长整数范围时转换失败。
     *
     * @param raw 原始数字
     * @return 长整数值
     */
    private static long toExactLong(Number raw) {
        try {
            return new BigDecimal(
                    raw.toString()
            ).longValueExact();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Expected an integral numeric value: " + raw,
                    e
            );
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(
                    "Expected an integral numeric value: " + raw,
                    e
            );
        }
    }

    /**
     * 将原始配置值转换为整数。
     *
     * @param raw 原始配置值
     * @return 整数配置值
     */
    private static Integer toInteger(Object raw) {
        requireRawValue(raw);

        if (raw instanceof Integer) {
            return (Integer) raw;
        }

        if (raw instanceof Number) {
            long value = toExactLong((Number) raw);

            if (value < Integer.MIN_VALUE
                    || value > Integer.MAX_VALUE) {
                throw new IllegalArgumentException(
                        "Integer value out of range: " + raw
                );
            }

            return Integer.valueOf((int) value);
        }

        return Integer.valueOf(
                raw.toString().trim()
        );
    }

    /**
     * 将原始配置值转换为长整数。
     *
     * @param raw 原始配置值
     * @return 长整数配置值
     */
    private static Long toLong(Object raw) {
        requireRawValue(raw);

        if (raw instanceof Long) {
            return (Long) raw;
        }

        if (raw instanceof Number) {
            return Long.valueOf(
                    toExactLong((Number) raw)
            );
        }

        return Long.valueOf(
                raw.toString().trim()
        );
    }

    /**
     * 将原始配置值转换为单精度浮点数。
     *
     * @param raw 原始配置值
     * @return 单精度浮点数配置值
     */
    private static Float toFloat(Object raw) {
        requireRawValue(raw);

        if (raw instanceof Float) {
            return (Float) raw;
        }

        if (raw instanceof Number) {
            return Float.valueOf(
                    ((Number) raw).floatValue()
            );
        }

        return Float.valueOf(
                raw.toString().trim()
        );
    }

    /**
     * 将原始配置值转换为双精度浮点数。
     *
     * @param raw 原始配置值
     * @return 双精度浮点数配置值
     */
    private static Double toDouble(Object raw) {
        requireRawValue(raw);

        if (raw instanceof Double) {
            return (Double) raw;
        }

        if (raw instanceof Number) {
            return Double.valueOf(
                    ((Number) raw).doubleValue()
            );
        }

        return Double.valueOf(
                raw.toString().trim()
        );
    }

    /**
     * 将原始配置值转换为高精度小数。
     *
     * @param raw 原始配置值
     * @return 高精度小数配置值
     */
    private static BigDecimal toBigDecimal(Object raw) {
        requireRawValue(raw);

        if (raw instanceof BigDecimal) {
            return (BigDecimal) raw;
        }

        if (raw instanceof Number) {
            return new BigDecimal(raw.toString());
        }

        return new BigDecimal(
                raw.toString().trim()
        );
    }

    /**
     * 将原始配置值转换为布尔值。
     *
     * 仅接受忽略大小写的 {@code true} 或 {@code false}。
     *
     * @param raw 原始配置值
     * @return 布尔配置值
     */
    private static Boolean toBoolean(Object raw) {
        requireRawValue(raw);

        if (raw instanceof Boolean) {
            return (Boolean) raw;
        }

        String value = raw.toString().trim();

        if ("true".equalsIgnoreCase(value)) {
            return Boolean.TRUE;
        }

        if ("false".equalsIgnoreCase(value)) {
            return Boolean.FALSE;
        }

        throw new IllegalArgumentException(
                "Boolean value must be true or false"
        );
    }

    /**
     * 将原始配置值转换为时间间隔。
     *
     * 支持 ISO-8601 格式，以及 {@code ms}、{@code s}、{@code m}、
     * {@code h}、{@code d} 后缀的简写格式。
     *
     * @param raw 原始配置值
     * @return 时间间隔配置值
     */
    private static Duration toDuration(Object raw) {
        requireRawValue(raw);

        if (raw instanceof Duration) {
            return (Duration) raw;
        }

        String value = raw.toString().trim();

        if (value.isEmpty()) {
            throw new IllegalArgumentException(
                    "Duration value must not be blank"
            );
        }

        try {
            return Duration.parse(value);
        } catch (RuntimeException ignored) {
            // 继续尝试解析简写时间格式。
        }

        String normalized =
                value.replace(" ", "")
                        .toLowerCase(Locale.ROOT);

        if (normalized.endsWith("ms")) {
            return Duration.ofMillis(
                    parseLong(normalized, 2)
            );
        }

        if (normalized.endsWith("s")) {
            return Duration.ofSeconds(
                    parseLong(normalized, 1)
            );
        }

        if (normalized.endsWith("m")) {
            return Duration.ofMinutes(
                    parseLong(normalized, 1)
            );
        }

        if (normalized.endsWith("h")) {
            return Duration.ofHours(
                    parseLong(normalized, 1)
            );
        }

        if (normalized.endsWith("d")) {
            return Duration.ofDays(
                    parseLong(normalized, 1)
            );
        }

        throw new IllegalArgumentException(
                "Unsupported duration value: " + value
        );
    }

    /**
     * 移除时间单位后缀并解析长整数。
     *
     * @param value        待解析文本
     * @param suffixLength 后缀长度
     * @return 长整数值
     */
    private static long parseLong(
            String value,
            int suffixLength) {

        String numberPart =
                value.substring(
                        0,
                        value.length() - suffixLength
                );

        return Long.parseLong(numberPart);
    }

    /**
     * 将原始配置值转换为指定枚举值。
     *
     * 枚举名称和 {@link Enum#toString()} 返回值均忽略大小写匹配。
     *
     * @param raw      原始配置值
     * @param enumType 枚举类型
     * @param <E>      枚举值类型
     * @return 枚举配置值
     */
    private static <E extends Enum<E>> E toEnum(
            Object raw,
            Class<E> enumType) {

        requireRawValue(raw);

        String value = raw.toString().trim();
        E[] enumConstants = enumType.getEnumConstants();

        for (E enumConstant : enumConstants) {
            if (enumConstant.name().equalsIgnoreCase(value)
                    || enumConstant.toString()
                    .equalsIgnoreCase(value)) {
                return enumConstant;
            }
        }

        throw new IllegalArgumentException(
                "Expected one of "
                        + Arrays.toString(enumConstants)
                        + ", but was: "
                        + value
        );
    }

    /**
     * 将原始配置值转换为列表。
     *
     * 支持集合类型以及使用逗号分隔的字符串。
     *
     * @param raw           原始配置值
     * @param itemConverter 列表元素转换器
     * @param <T>           列表元素类型
     * @return 不可修改的配置值列表
     */
    private static <T> List<T> toList(
            Object raw,
            Function<Object, T> itemConverter) {

        requireRawValue(raw);
        Objects.requireNonNull(
                itemConverter,
                "itemConverter must not be null"
        );

        List<T> result = new ArrayList<>();

        if (raw instanceof Collection) {
            Collection<?> values = (Collection<?>) raw;

            for (Object value : values) {
                result.add(
                        itemConverter.apply(value)
                );
            }

            return Collections.unmodifiableList(result);
        }

        if (raw instanceof String) {
            String value = ((String) raw).trim();

            if (value.isEmpty()) {
                return Collections.emptyList();
            }

            String[] items = value.split(",");

            for (String item : items) {
                result.add(
                        itemConverter.apply(item.trim())
                );
            }

            return Collections.unmodifiableList(result);
        }

        throw new IllegalArgumentException(
                "Expected a list, but was: "
                        + raw.getClass().getName()
        );
    }

    /**
     * 将原始配置值转换为字符串键值映射。
     *
     * Map 的键和值均通过 {@link String#valueOf(Object)} 转换为字符串。
     *
     * @param raw 原始配置值
     * @return 不可修改的字符串键值映射
     */
    private static Map<String, String> toStringMap(
            Object raw) {

        requireRawValue(raw);

        if (!(raw instanceof Map)) {
            throw new IllegalArgumentException(
                    "Expected a map, but was: "
                            + raw.getClass().getName()
            );
        }

        Map<?, ?> source = (Map<?, ?>) raw;
        Map<String, String> result =
                new LinkedHashMap<>();

        for (Map.Entry<?, ?> entry : source.entrySet()) {
            result.put(
                    String.valueOf(entry.getKey()),
                    String.valueOf(entry.getValue())
            );
        }

        return Collections.unmodifiableMap(result);
    }

    /**
     * 将原始配置值转换为对象键值映射。
     *
     * Map 的键通过 {@link String#valueOf(Object)} 转换为字符串，
     * 值保持原始对象不变。
     *
     * @param raw 原始配置值
     * @return 不可修改的对象键值映射
     */
    private static Map<String, Object> toObjectMap(
            Object raw) {

        requireRawValue(raw);

        if (!(raw instanceof Map)) {
            throw new IllegalArgumentException(
                    "Expected a map, but was: "
                            + raw.getClass().getName()
            );
        }

        Map<?, ?> source = (Map<?, ?>) raw;
        Map<String, Object> result =
                new LinkedHashMap<>();

        for (Map.Entry<?, ?> entry : source.entrySet()) {
            result.put(
                    String.valueOf(entry.getKey()),
                    entry.getValue()
            );
        }

        return Collections.unmodifiableMap(result);
    }

    /**
     * 校验原始配置值不为空。
     *
     * @param raw 原始配置值
     */
    private static void requireRawValue(Object raw) {
        if (raw == null) {
            throw new IllegalArgumentException(
                    "Raw configuration value must not be null"
            );
        }
    }

    /**
     * 判断文本是否为空白。
     *
     * @param value 待判断文本
     * @return 文本为 {@code null} 或空白时返回 {@code true}
     */
    private static boolean isBlank(String value) {
        return value == null
                || value.trim().isEmpty();
    }
}
