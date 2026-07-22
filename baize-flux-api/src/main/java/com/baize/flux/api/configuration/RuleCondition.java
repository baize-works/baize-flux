package com.baize.flux.api.configuration;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Immutable condition used to activate conditional option rules.
 */
public final class RuleCondition {

    private final String description;
    private final Predicate<ReadonlyConfig> predicate;
    private final Set<Option<?>> referencedOptions;

    private RuleCondition(
            String description,
            Predicate<ReadonlyConfig> predicate,
            Set<Option<?>> referencedOptions) {
        this.description = Objects.requireNonNull(description, "description");
        this.predicate = Objects.requireNonNull(predicate, "predicate");
        this.referencedOptions = Collections.unmodifiableSet(
                new LinkedHashSet<Option<?>>(referencedOptions));
    }

    public static <T> RuleCondition equalTo(Option<T> option, T expectedValue) {
        Objects.requireNonNull(option, "option");
        return new RuleCondition(
                "'" + option.key() + "' == " + expectedValue,
                config ->
                        config.getResolvedOptional(option)
                                .map(value -> Objects.equals(value, expectedValue))
                                .orElse(false),
                Collections.<Option<?>>singleton(option));
    }

    public static RuleCondition present(Option<?> option) {
        Objects.requireNonNull(option, "option");
        return new RuleCondition(
                "'" + option.key() + "' is configured", config -> config.contains(option), Collections.<Option<?>>singleton(option));
    }

    public static RuleCondition absent(Option<?> option) {
        Objects.requireNonNull(option, "option");
        return new RuleCondition(
                "'" + option.key() + "' is not configured",
                config -> !config.contains(option),
                Collections.<Option<?>>singleton(option));
    }

    public static RuleCondition custom(
            String description,
            Set<Option<?>> referencedOptions,
            Predicate<ReadonlyConfig> predicate) {
        return new RuleCondition(description, predicate, referencedOptions);
    }

    public RuleCondition and(RuleCondition other) {
        Objects.requireNonNull(other, "other");
        Set<Option<?>> references = merge(referencedOptions, other.referencedOptions);
        return new RuleCondition(
                "(" + description + " && " + other.description + ")",
                config -> matches(config) && other.matches(config),
                references);
    }

    public RuleCondition or(RuleCondition other) {
        Objects.requireNonNull(other, "other");
        Set<Option<?>> references = merge(referencedOptions, other.referencedOptions);
        return new RuleCondition(
                "(" + description + " || " + other.description + ")",
                config -> matches(config) || other.matches(config),
                references);
    }

    public RuleCondition negate() {
        return new RuleCondition(
                "!(" + description + ")", config -> !matches(config), referencedOptions);
    }

    public boolean matches(ReadonlyConfig config) {
        return predicate.test(config);
    }

    public String description() {
        return description;
    }

    public Set<Option<?>> referencedOptions() {
        return referencedOptions;
    }

    private static Set<Option<?>> merge(Set<Option<?>> left, Set<Option<?>> right) {
        Set<Option<?>> result = new LinkedHashSet<>(left);
        result.addAll(right);
        return result;
    }
}
