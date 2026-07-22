package com.baize.flux.api.configuration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Aggregated configuration validation result.
 *
 * <p>This result can be used by CLI, REST API and Web UI.</p>
 */
public final class ValidationResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final ValidationResult VALID_RESULT =
            new ValidationResult(Collections.<Violation>emptyList());

    private final List<Violation> violations;

    public ValidationResult(List<Violation> violations) {
        Objects.requireNonNull(violations, "violations must not be null");

        List<Violation> copiedViolations =
                new ArrayList<Violation>(violations.size());

        for (Violation violation : violations) {
            copiedViolations.add(
                    Objects.requireNonNull(
                            violation,
                            "violation must not be null"
                    )
            );
        }

        this.violations =
                Collections.unmodifiableList(copiedViolations);
    }

    /**
     * Returns a valid result without any violations.
     */
    public static ValidationResult valid() {
        return VALID_RESULT;
    }

    /**
     * Creates a validation result containing one violation.
     */
    public static ValidationResult of(Violation violation) {
        return new ValidationResult(
                Collections.singletonList(
                        Objects.requireNonNull(
                                violation,
                                "violation must not be null"
                        )
                )
        );
    }

    /**
     * Creates a validation result containing multiple violations.
     */
    public static ValidationResult of(List<Violation> violations) {
        return new ValidationResult(violations);
    }

    /**
     * Creates a builder used to collect violations.
     */
    public static Builder builder() {
        return new Builder();
    }

    public List<Violation> violations() {
        return violations;
    }

    public boolean isValid() {
        return violations.isEmpty();
    }

    public boolean isInvalid() {
        return !isValid();
    }

    public int violationCount() {
        return violations.size();
    }

    public void throwIfInvalid() {
        if (isInvalid()) {
            throw new ConfigValidationException(this);
        }
    }

    @Override
    public String toString() {
        return "ValidationResult{"
                + "valid="
                + isValid()
                + ", violations="
                + violations
                + '}';
    }

    /**
     * Configuration validation violation type.
     */
    public enum ViolationType {

        /**
         * An undeclared configuration key was provided.
         */
        UNKNOWN_KEY,

        /**
         * The configuration value cannot be converted to the expected type.
         */
        TYPE_MISMATCH,

        /**
         * The configuration value is outside the allowed value set.
         */
        ALLOWED_VALUES,

        /**
         * A required configuration option is missing.
         */
        REQUIRED,

        /**
         * Exactly one option in the group must be configured.
         */
        EXACTLY_ONE,

        /**
         * At most one option in the group can be configured.
         */
        AT_MOST_ONE,

        /**
         * All options must be configured together or omitted together.
         */
        ALL_OR_NONE,

        /**
         * An option required by a condition is missing.
         */
        CONDITIONAL_REQUIRED,

        /**
         * A configuration value does not satisfy its declared constraint.
         */
        VALUE_CONSTRAINT
    }

    /**
     * Describes one configuration validation violation.
     */
    public static final class Violation implements Serializable {

        private static final long serialVersionUID = 1L;

        private final ViolationType type;
        private final List<String> optionKeys;
        private final String message;

        public Violation(
                ViolationType type,
                List<String> optionKeys,
                String message) {

            this.type =
                    Objects.requireNonNull(
                            type,
                            "violation type must not be null"
                    );

            this.optionKeys = normalizeOptionKeys(optionKeys);
            this.message = requireNonBlank(message, "message");
        }

        /**
         * Creates a violation related to one option.
         */
        public static Violation of(
                ViolationType type,
                String optionKey,
                String message) {

            return new Violation(
                    type,
                    Collections.singletonList(
                            requireNonBlank(optionKey, "optionKey")
                    ),
                    message
            );
        }

        /**
         * Creates a violation related to multiple options.
         */
        public static Violation of(
                ViolationType type,
                List<String> optionKeys,
                String message) {

            return new Violation(type, optionKeys, message);
        }

        /**
         * Creates a global violation not associated with one option.
         */
        public static Violation global(
                ViolationType type,
                String message) {

            return new Violation(
                    type,
                    Collections.<String>emptyList(),
                    message
            );
        }

        public ViolationType type() {
            return type;
        }

        public List<String> optionKeys() {
            return optionKeys;
        }

        public String message() {
            return message;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (!(obj instanceof Violation)) {
                return false;
            }

            Violation that = (Violation) obj;

            return type == that.type
                    && Objects.equals(optionKeys, that.optionKeys)
                    && Objects.equals(message, that.message);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, optionKeys, message);
        }

        @Override
        public String toString() {
            return "Violation{"
                    + "type="
                    + type
                    + ", optionKeys="
                    + optionKeys
                    + ", message='"
                    + message
                    + '\''
                    + '}';
        }
    }

    /**
     * Builder used by validators to collect violations.
     */
    public static final class Builder {

        private final List<Violation> violations =
                new ArrayList<Violation>();

        private Builder() {
        }

        public Builder add(Violation violation) {
            violations.add(
                    Objects.requireNonNull(
                            violation,
                            "violation must not be null"
                    )
            );
            return this;
        }

        public Builder add(
                ViolationType type,
                String optionKey,
                String message) {

            return add(
                    Violation.of(type, optionKey, message)
            );
        }

        public Builder add(
                ViolationType type,
                List<String> optionKeys,
                String message) {

            return add(
                    Violation.of(type, optionKeys, message)
            );
        }

        public Builder addGlobal(
                ViolationType type,
                String message) {

            return add(
                    Violation.global(type, message)
            );
        }

        public Builder addAll(
                Collection<Violation> violations) {

            Objects.requireNonNull(
                    violations,
                    "violations must not be null"
            );

            for (Violation violation : violations) {
                add(violation);
            }

            return this;
        }

        public boolean isEmpty() {
            return violations.isEmpty();
        }

        public int size() {
            return violations.size();
        }

        public ValidationResult build() {
            if (violations.isEmpty()) {
                return ValidationResult.valid();
            }

            return new ValidationResult(violations);
        }
    }

    private static List<String> normalizeOptionKeys(
            List<String> optionKeys) {

        Objects.requireNonNull(
                optionKeys,
                "optionKeys must not be null"
        );

        Set<String> normalizedKeys =
                new LinkedHashSet<String>();

        for (String optionKey : optionKeys) {
            normalizedKeys.add(
                    requireNonBlank(optionKey, "optionKey")
            );
        }

        return Collections.unmodifiableList(
                new ArrayList<String>(normalizedKeys)
        );
    }

    private static String requireNonBlank(
            String value,
            String fieldName) {

        Objects.requireNonNull(
                value,
                fieldName + " must not be null"
        );

        String normalizedValue = value.trim();

        if (normalizedValue.isEmpty()) {
            throw new IllegalArgumentException(
                    fieldName + " must not be blank"
            );
        }

        return normalizedValue;
    }
}