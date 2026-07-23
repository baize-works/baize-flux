package com.baize.flux.api.configuration.util;


import com.baize.flux.api.configuration.ReadonlyConfig;

/**
 * Pluggable validation extension for cases where built-in {@link ConditionOperator} operators are
 * not expressive enough — for example, validating the internal structure of a {@code List<Map>} or
 * enforcing cross-key constraints inside nested configs.
 *
 * <p>Wire an implementation via {@link
 * Conditions#extension(org.apache.seatunnel.api.configuration.Option, ConditionExtension)}. The
 * extension plugs into the same {@code valueConstraints} pipeline as all built-in operators and
 * supports {@code .and()} / {@code .or()} chaining, {@code required}, {@code optional}, and {@code
 * conditional} rules.
 *
 * <p>Implementations should avoid I/O (database connections, HTTP calls, file access) and only
 * validate structure and values. {@link #evaluate} runs only during job submission validation; REST
 * metadata queries only serialize {@link #description()}.
 *
 * @param <T> the option value type
 */
public interface ConditionExtension<T> {

    /**
     * Rule description used in error messages ({@link Condition#toString()}) and metadata
     * serialization (REST {@code /option-rules} and CLI metadata export).
     *
     * @return non-null description, e.g. {@code "must be between 1 and 65535"}
     */
    String description();

    /**
     * Evaluates whether {@code value} passes this validation rule.
     *
     * <p>Return {@code false} for simple failure — the framework composes the error from {@link
     * #description()} automatically and continues collecting other validation errors.
     *
     * <p>Throw {@link OptionValidationException} when a richer, context-specific message is needed.
     * The framework catches the exception, extracts its message, and adds it to the aggregated
     * error list — subsequent validations still run. Use this when you need a more descriptive
     * error message than {@link #description()} alone provides.
     *
     * <p>Avoid other unchecked exceptions — they propagate unwrapped.
     *
     * @param config full configuration context (read-only), available for cross-field checks
     * @param value the resolved option value; may be {@code null}
     * @return {@code true} if valid
     * @throws OptionValidationException for detailed, context-specific error reporting
     */
    boolean evaluate(ReadonlyConfig config, T value) throws OptionValidationException;
}
