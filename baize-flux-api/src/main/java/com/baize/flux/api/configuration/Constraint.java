package com.baize.flux.api.configuration;

import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * Value-level validation constraint for an option.
 */
public final class Constraint<T> {

    private final String description;
    private final BiPredicate<ReadonlyConfig, T> predicate;

    private Constraint(String description, BiPredicate<ReadonlyConfig, T> predicate) {
        this.description = Objects.requireNonNull(description, "description");
        this.predicate = Objects.requireNonNull(predicate, "predicate");
    }

    public static <T> Constraint<T> of(
            String description, BiPredicate<ReadonlyConfig, T> predicate) {
        return new Constraint<>(description, predicate);
    }

    public boolean test(ReadonlyConfig config, T value) {
        return predicate.test(config, value);
    }

    public String description() {
        return description;
    }
}
