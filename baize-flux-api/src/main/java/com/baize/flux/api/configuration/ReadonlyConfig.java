package com.baize.flux.api.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable, typed view over raw configuration values.
 */
public final class ReadonlyConfig {

    private final Map<String, Object> values;

    private ReadonlyConfig(Map<String, Object> values) {
        this.values = deepImmutableMap(values);
    }

    public static ReadonlyConfig fromMap(Map<String, Object> values) {
        return new ReadonlyConfig(Objects.requireNonNull(values, "values"));
    }

    /**
     * Reads an explicitly configured value, including fallback keys, but does not apply defaults.
     */
    public <T> Optional<T> getOptional(Option<T> option) {
        Objects.requireNonNull(option, "option");
        Object rawValue = findValue(option.key());
        if (rawValue == null) {
            for (String fallbackKey : option.fallbackKeys()) {
                rawValue = findValue(fallbackKey);
                if (rawValue != null) {
                    break;
                }
            }
        }
        return rawValue == null ? Optional.empty() : Optional.of(option.convert(rawValue));
    }

    /**
     * Reads a configured value and falls back to the option's default value.
     */
    public <T> Optional<T> getResolvedOptional(Option<T> option) {
        Optional<T> configured = getOptional(option);
        if (configured.isPresent()) {
            return configured;
        }
        return option.defaultValueOptional();
    }

    /**
     * Reads a configured or default value. Throws when the option is absent and has no default.
     */
    public <T> T get(Option<T> option) {
        return getResolvedOptional(option)
                .orElseThrow(
                        () ->
                                new ConfigAccessException(
                                        "Option '" + option.key() + "' is not configured"));
    }

    public boolean contains(Option<?> option) {
        if (findValue(option.key()) != null) {
            return true;
        }
        return option.fallbackKeys().stream().anyMatch(key -> findValue(key) != null);
    }

    public boolean contains(String key) {
        return findValue(key) != null;
    }

    public Optional<Object> getRaw(String key) {
        return Optional.ofNullable(findValue(key));
    }

    public Map<String, Object> asMap() {
        return values;
    }

    private Object findValue(String key) {
        if (values.containsKey(key)) {
            return values.get(key);
        }

        String[] parts = key.split("\\.");
        Object current = values;
        for (String part : parts) {
            if (!(current instanceof Map<?, ?> map)) {
                return null;
            }
            current = map.get(part);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    private static Map<String, Object> deepImmutableMap(Map<?, ?> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        source.forEach((key, value) -> result.put(String.valueOf(key), deepImmutableValue(value)));
        return Collections.unmodifiableMap(result);
    }

    private static Object deepImmutableValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            return deepImmutableMap(map);
        }
        if (value instanceof List<?> list) {
            List<Object> result = new ArrayList<>(list.size());
            list.forEach(item -> result.add(deepImmutableValue(item)));
            return Collections.unmodifiableList(result);
        }
        return value;
    }
}
