package com.baize.flux.api.configuration;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Common value constraints. Connector-specific validation can use {@link Constraint#of}.
 */
public final class Constraints {

    private Constraints() {
    }

    public static Constraint<String> notBlank() {
        return Constraint.of("must not be blank", (config, value) -> value != null && !value.trim().isEmpty());
    }

    public static Constraint<String> matches(String regex) {
        Pattern pattern = Pattern.compile(regex);
        return Constraint.of(
                "must match pattern " + regex,
                (config, value) -> value != null && pattern.matcher(value).matches());
    }

    public static <T extends Comparable<T>> Constraint<T> greaterOrEqual(T minimum) {
        return Constraint.of(
                "must be greater than or equal to " + minimum,
                (config, value) -> value != null && value.compareTo(minimum) >= 0);
    }

    public static <T extends Comparable<T>> Constraint<T> lessOrEqual(T maximum) {
        return Constraint.of(
                "must be less than or equal to " + maximum,
                (config, value) -> value != null && value.compareTo(maximum) <= 0);
    }

    public static <T extends Comparable<T>> Constraint<T> between(T minimum, T maximum) {
        return Constraint.of(
                "must be between " + minimum + " and " + maximum,
                (config, value) ->
                        value != null
                                && value.compareTo(minimum) >= 0
                                && value.compareTo(maximum) <= 0);
    }

    public static <T extends Collection<?>> Constraint<T> notEmptyCollection() {
        return Constraint.of("must not be empty", (config, value) -> value != null && !value.isEmpty());
    }

    public static <T extends Collection<?>> Constraint<T> uniqueCollection() {
        return Constraint.of(
                "must contain unique values",
                (config, value) -> value != null && value.size() == new HashSet<>(value).size());
    }

    public static <T extends Map<?, ?>> Constraint<T> notEmptyMap() {
        return Constraint.of("must not be empty", (config, value) -> value != null && !value.isEmpty());
    }
}
