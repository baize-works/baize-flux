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
 * Factory and fluent builder for {@link Option}.
 */
public final class Options {

    private Options() {
    }

    public static OptionBuilder key(String key) {
        if (isBlank(key)) {
            throw new IllegalArgumentException("Option key must not be blank");
        }
        return new OptionBuilder(key);
    }

    public static final class OptionBuilder {

        private final String key;

        private OptionBuilder(String key) {
            this.key = key;
        }

        public TypedOptionBuilder<String> stringType() {
            return typed(
                    "String",
                    new ConfigConverter<String>() {
                        @Override
                        public String convert(Object raw) {
                            return String.valueOf(raw);
                        }
                    },
                    false
            );
        }

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

        public <E extends Enum<E>> TypedOptionBuilder<E> enumType(
                final Class<E> enumType) {

            Objects.requireNonNull(enumType, "enumType must not be null");

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
                                            return String.valueOf(value);
                                        }
                                    }
                            );
                        }
                    },
                    false
            );
        }

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
                                            return elementConverter.convert(value);
                                        }
                                    }
                            );
                        }
                    },
                    false
            );
        }

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
         * Defines a connector-specific type without making the configuration
         * core depend on a JSON mapper or reflection framework.
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

            return typed(typeName, converter, false);
        }

        private <T> TypedOptionBuilder<T> typed(
                String typeName,
                ConfigConverter<T> converter,
                boolean allowNestedKeys) {

            return new TypedOptionBuilder<T>(
                    key,
                    typeName,
                    converter,
                    allowNestedKeys
            );
        }
    }

    public static final class TypedOptionBuilder<T> {

        private final String key;
        private final String typeName;
        private final ConfigConverter<T> converter;

        private String description = "";
        private final List<String> fallbackKeys =
                new ArrayList<String>();
        private final List<T> allowedValues =
                new ArrayList<T>();

        private boolean sensitive;
        private boolean allowNestedKeys;

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

        public TypedOptionBuilder<T> description(
                String description) {

            this.description =
                    description == null ? "" : description;

            return this;
        }

        public TypedOptionBuilder<T> fallbackKeys(
                String... fallbackKeys) {

            if (fallbackKeys == null) {
                return this;
            }

            for (String fallbackKey : fallbackKeys) {
                if (!isBlank(fallbackKey)) {
                    this.fallbackKeys.add(fallbackKey.trim());
                }
            }

            return this;
        }

        @SafeVarargs
        public final TypedOptionBuilder<T> allowedValues(
                T... values) {

            if (values != null) {
                this.allowedValues.addAll(Arrays.asList(values));
            }

            return this;
        }

        public TypedOptionBuilder<T> allowedValues(
                Collection<T> values) {

            if (values != null) {
                this.allowedValues.addAll(values);
            }

            return this;
        }

        public TypedOptionBuilder<T> sensitive() {
            this.sensitive = true;
            return this;
        }

        /**
         * Allows arbitrary nested keys, useful for properties-style map options.
         */
        public TypedOptionBuilder<T> allowNestedKeys() {
            this.allowNestedKeys = true;
            return this;
        }

        public Option<T> defaultValue(T value) {
            return build(value, true);
        }

        public Option<T> noDefaultValue() {
            return build(null, false);
        }

        private Option<T> build(
                T defaultValue,
                boolean hasDefaultValue) {

            return new Option<T>(
                    key,
                    typeName,
                    converter,
                    defaultValue,
                    hasDefaultValue,
                    description,
                    new ArrayList<String>(fallbackKeys),
                    new ArrayList<T>(allowedValues),
                    sensitive,
                    allowNestedKeys
            );
        }
    }

    private static Integer toInteger(Object raw) {
        requireRawValue(raw);

        if (raw instanceof Integer) {
            return (Integer) raw;
        }

        if (raw instanceof Number) {
            long value = ((Number) raw).longValue();

            if (value < Integer.MIN_VALUE
                    || value > Integer.MAX_VALUE) {
                throw new IllegalArgumentException(
                        "Integer value out of range: " + raw
                );
            }

            return Integer.valueOf((int) value);
        }

        return Integer.valueOf(raw.toString().trim());
    }

    private static Long toLong(Object raw) {
        requireRawValue(raw);

        if (raw instanceof Long) {
            return (Long) raw;
        }

        if (raw instanceof Number) {
            return Long.valueOf(((Number) raw).longValue());
        }

        return Long.valueOf(raw.toString().trim());
    }

    private static Float toFloat(Object raw) {
        requireRawValue(raw);

        if (raw instanceof Float) {
            return (Float) raw;
        }

        if (raw instanceof Number) {
            return Float.valueOf(((Number) raw).floatValue());
        }

        return Float.valueOf(raw.toString().trim());
    }

    private static Double toDouble(Object raw) {
        requireRawValue(raw);

        if (raw instanceof Double) {
            return (Double) raw;
        }

        if (raw instanceof Number) {
            return Double.valueOf(((Number) raw).doubleValue());
        }

        return Double.valueOf(raw.toString().trim());
    }

    private static BigDecimal toBigDecimal(Object raw) {
        requireRawValue(raw);

        if (raw instanceof BigDecimal) {
            return (BigDecimal) raw;
        }

        if (raw instanceof Number) {
            return new BigDecimal(raw.toString());
        }

        return new BigDecimal(raw.toString().trim());
    }

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
            // Try shorthand duration formats below.
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

    private static <T> List<T> toList(
            Object raw,
            Function<Object, T> itemConverter) {

        requireRawValue(raw);
        Objects.requireNonNull(
                itemConverter,
                "itemConverter must not be null"
        );

        List<T> result = new ArrayList<T>();

        if (raw instanceof Collection) {
            Collection<?> values = (Collection<?>) raw;

            for (Object value : values) {
                result.add(itemConverter.apply(value));
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
                new LinkedHashMap<String, String>();

        for (Map.Entry<?, ?> entry : source.entrySet()) {
            result.put(
                    String.valueOf(entry.getKey()),
                    String.valueOf(entry.getValue())
            );
        }

        return Collections.unmodifiableMap(result);
    }

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
                new LinkedHashMap<String, Object>();

        for (Map.Entry<?, ?> entry : source.entrySet()) {
            result.put(
                    String.valueOf(entry.getKey()),
                    entry.getValue()
            );
        }

        return Collections.unmodifiableMap(result);
    }

    private static void requireRawValue(Object raw) {
        if (raw == null) {
            throw new IllegalArgumentException(
                    "Raw configuration value must not be null"
            );
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}