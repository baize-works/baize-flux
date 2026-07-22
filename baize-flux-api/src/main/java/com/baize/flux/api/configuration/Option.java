package com.baize.flux.api.configuration;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Metadata describing one configuration item.
 *
 * <p>An option only describes key, type conversion and presentation metadata. Whether the option
 * is required belongs to {@link OptionRule}, not to this class.
 */
public final class Option<T> {

    private final String key;
    private final String typeName;
    private final ConfigConverter<T> converter;
    private final T defaultValue;
    private final boolean hasDefaultValue;
    private final String description;
    private final List<String> fallbackKeys;
    private final List<T> allowedValues;
    private final boolean sensitive;
    private final boolean allowNestedKeys;

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
        this.key = requireText(key, "Option key must not be blank");
        this.typeName = requireText(typeName, "Option type name must not be blank");
        this.converter = Objects.requireNonNull(converter, "converter");
        this.defaultValue = defaultValue;
        this.hasDefaultValue = hasDefaultValue;
        this.description = description == null ? "" : description;
        this.fallbackKeys = List.copyOf(fallbackKeys);
        this.allowedValues = List.copyOf(allowedValues);
        this.sensitive = sensitive;
        this.allowNestedKeys = allowNestedKeys;

        if (hasDefaultValue
                && !this.allowedValues.isEmpty()
                && !this.allowedValues.contains(defaultValue)) {
            throw new IllegalArgumentException(
                    "Default value of option '"
                            + key
                            + "' must be one of "
                            + this.allowedValues);
        }
        if (this.fallbackKeys.contains(key)) {
            throw new IllegalArgumentException(
                    "Fallback keys of option '" + key + "' must not contain itself");
        }
    }

    public String key() {
        return key;
    }

    public String typeName() {
        return typeName;
    }

    ConfigConverter<T> converter() {
        return converter;
    }

    public boolean hasDefaultValue() {
        return hasDefaultValue;
    }

    public T defaultValue() {
        return defaultValue;
    }

    public Optional<T> defaultValueOptional() {
        return hasDefaultValue ? Optional.ofNullable(defaultValue) : Optional.empty();
    }

    public String description() {
        return description;
    }

    public List<String> fallbackKeys() {
        return fallbackKeys;
    }

    public List<T> allowedValues() {
        return allowedValues;
    }

    public boolean sensitive() {
        return sensitive;
    }

    /**
     * Whether unknown-key validation should allow arbitrary child keys under this option.
     */
    public boolean allowNestedKeys() {
        return allowNestedKeys;
    }

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
                    "Unable to convert option '" + key + "' to " + typeName,
                    e);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Option)) {
            return false;
        }

        Option<?> other = (Option<?>) obj;
        return key.equals(other.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String toString() {
        return "Option{" + "key='" + key + '\'' + ", type='" + typeName + '\'' + '}';
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}
