package com.baize.flux.api.configuration;

import com.baize.flux.common.exception.FluxException;

import java.io.Serial;
import java.util.Map;
import java.util.Objects;

/**
 * Exception thrown when configuration validation fails.
 *
 * <p>The complete validation result is retained so callers can inspect
 * individual violations programmatically.</p>
 */
public class ConfigValidationException extends FluxException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final ValidationResult validationResult;

    public ConfigValidationException(ValidationResult validationResult) {
        this(validationResult, null);
    }

    public ConfigValidationException(
            ValidationResult validationResult,
            Throwable cause) {
        this(validate(validationResult), cause, true);
    }

    private ConfigValidationException(
            ValidationResult validationResult,
            Throwable cause,
            boolean ignored) {
        super(
                ConfigurationErrorCode.CONFIG_VALIDATION_FAILED,
                buildMessage(validationResult),
                cause,
                buildContext(validationResult)
        );
        this.validationResult = validationResult;
    }

    /**
     * Returns the complete validation result.
     */
    public ValidationResult validationResult() {
        return validationResult;
    }

    private static ValidationResult validate(
            ValidationResult validationResult) {
        return Objects.requireNonNull(
                validationResult,
                "validationResult must not be null"
        );
    }

    private static Map<String, Object> buildContext(
            ValidationResult validationResult) {
        return Map.of(
                "violationCount",
                validationResult.violations().size()
        );
    }

    private static String buildMessage(
            ValidationResult validationResult) {
        StringBuilder message = new StringBuilder(128);

        message.append("Configuration validation failed with ")
                .append(validationResult.violations().size())
                .append(" violation(s).");

        for (int index = 0;
             index < validationResult.violations().size();
             index++) {

            ValidationResult.Violation violation =
                    validationResult.violations().get(index);

            message.append(System.lineSeparator())
                    .append("  [")
                    .append(index + 1)
                    .append("] type=")
                    .append(violation.type())
                    .append(", options=")
                    .append(violation.optionKeys())
                    .append(", message=")
                    .append(violation.message());
        }

        return message.toString();
    }
}