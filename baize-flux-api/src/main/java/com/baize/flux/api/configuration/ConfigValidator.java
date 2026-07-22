package com.baize.flux.api.configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.baize.flux.api.configuration.ValidationResult.Violation;
import static com.baize.flux.api.configuration.ValidationResult.ViolationType;

/**
 * Validates unknown keys, type conversion, structural rules and value constraints.
 */
public final class ConfigValidator {

    private final boolean rejectUnknownKeys;

    private ConfigValidator(boolean rejectUnknownKeys) {
        this.rejectUnknownKeys = rejectUnknownKeys;
    }

    public static ConfigValidator strict() {
        return new ConfigValidator(true);
    }

    public static ConfigValidator lenient() {
        return new ConfigValidator(false);
    }

    public ValidationResult validate(
            ReadonlyConfig config,
            OptionRule optionRule) {

        List<Violation> violations = new ArrayList<Violation>();
        Set<String> typeErrorKeys = new HashSet<String>();

        if (rejectUnknownKeys) {
            validateUnknownKeys(config, optionRule, violations);
        }

        validateTypesAndAllowedValues(
                config,
                optionRule,
                violations,
                typeErrorKeys
        );

        validateRules(
                config,
                optionRule,
                violations,
                typeErrorKeys
        );

        return new ValidationResult(violations);
    }

    private void validateUnknownKeys(
            ReadonlyConfig config,
            OptionRule optionRule,
            List<Violation> violations) {

        Map<String, Option<?>> declared =
                new LinkedHashMap<String, Option<?>>();

        for (Option<?> option : optionRule.options()) {
            if (!declared.containsKey(option.key())) {
                declared.put(option.key(), option);
            }
            for (String fallbackKey : option.fallbackKeys()) {
                if (!declared.containsKey(fallbackKey)) {
                    declared.put(fallbackKey, option);
                }
            }
        }

        collectUnknownKeys(
                config.asMap(),
                "",
                declared,
                violations
        );
    }

    private void collectUnknownKeys(
            Map<String, Object> values,
            String prefix,
            Map<String, Option<?>> declared,
            List<Violation> violations) {

        for (Map.Entry<String, Object> entry : values.entrySet()) {
            String path =
                    prefix.isEmpty()
                            ? entry.getKey()
                            : prefix + "." + entry.getKey();

            Object value = entry.getValue();
            Option<?> exactOption = declared.get(path);

            if (exactOption != null) {
                if (value instanceof Map
                        && !exactOption.allowNestedKeys()) {

                    collectUnknownKeys(
                            castMap((Map<?, ?>) value),
                            path,
                            declared,
                            violations
                    );
                }
                continue;
            }

            Option<?> nestedOwner =
                    findNestedOwner(path, declared.values());

            if (nestedOwner != null
                    && nestedOwner.allowNestedKeys()) {
                continue;
            }

            boolean hasDeclaredChild =
                    hasDeclaredChild(path, declared.keySet());

            if (hasDeclaredChild && value instanceof Map) {
                collectUnknownKeys(
                        castMap((Map<?, ?>) value),
                        path,
                        declared,
                        violations
                );
                continue;
            }

            violations.add(
                    new Violation(
                            ViolationType.UNKNOWN_KEY,
                            Collections.singletonList(path),
                            "Unknown configuration option"
                    )
            );
        }
    }

    private boolean hasDeclaredChild(
            String path,
            Set<String> declaredKeys) {

        String childPrefix = path + ".";

        for (String declaredKey : declaredKeys) {
            if (declaredKey.startsWith(childPrefix)) {
                return true;
            }
        }

        return false;
    }

    private Option<?> findNestedOwner(
            String path,
            Collection<Option<?>> options) {

        for (Option<?> option : options) {
            if (path.startsWith(option.key() + ".")) {
                return option;
            }
        }

        return null;
    }

    private void validateTypesAndAllowedValues(
            ReadonlyConfig config,
            OptionRule optionRule,
            List<Violation> violations,
            Set<String> typeErrorKeys) {

        for (Option<?> option : optionRule.options()) {
            if (!config.contains(option)
                    && !option.hasDefaultValue()) {
                continue;
            }

            try {
                Object value =
                        config.getResolvedOptional(option).orElse(null);

                if (!option.allowedValues().isEmpty()
                        && !option.allowedValues().contains(value)) {

                    violations.add(
                            new Violation(
                                    ViolationType.ALLOWED_VALUES,
                                    Collections.singletonList(option.key()),
                                    "Value must be one of "
                                            + option.allowedValues()
                            )
                    );
                }
            } catch (ConfigConversionException e) {
                typeErrorKeys.add(option.key());

                String message =
                        "Expected type " + e.expectedType();

                if (e.rawValueType() != null
                        && !e.rawValueType().trim().isEmpty()) {
                    message +=
                            ", but raw value type was "
                                    + e.rawValueType();
                }

                violations.add(
                        new Violation(
                                ViolationType.TYPE_MISMATCH,
                                Collections.singletonList(option.key()),
                                message
                        )
                );
            }
        }
    }

    private void validateRules(
            ReadonlyConfig config,
            OptionRule optionRule,
            List<Violation> violations,
            Set<String> typeErrorKeys) {

        for (OptionRule.Rule rule : optionRule.rules()) {
            if (rule instanceof OptionRule.RequiredRule) {
                validateRequired(
                        config,
                        (OptionRule.RequiredRule) rule,
                        violations
                );
            } else if (rule instanceof OptionRule.ExactlyOneRule) {
                validateExactlyOne(
                        config,
                        (OptionRule.ExactlyOneRule) rule,
                        violations
                );
            } else if (rule instanceof OptionRule.AtMostOneRule) {
                validateAtMostOne(
                        config,
                        (OptionRule.AtMostOneRule) rule,
                        violations
                );
            } else if (rule instanceof OptionRule.AllOrNoneRule) {
                validateAllOrNone(
                        config,
                        (OptionRule.AllOrNoneRule) rule,
                        violations
                );
            } else if (rule
                    instanceof OptionRule.ConditionalRequiredRule) {
                validateConditional(
                        config,
                        (OptionRule.ConditionalRequiredRule) rule,
                        violations
                );
            } else if (rule instanceof OptionRule.ValueRule) {
                validateValue(
                        config,
                        (OptionRule.ValueRule<?>) rule,
                        violations,
                        typeErrorKeys
                );
            }
        }
    }

    private void validateRequired(
            ReadonlyConfig config,
            OptionRule.RequiredRule rule,
            List<Violation> violations) {

        List<String> absent = new ArrayList<String>();

        for (Option<?> option : rule.options()) {
            if (!isResolved(config, option)) {
                absent.add(option.key());
            }
        }

        if (!absent.isEmpty()) {
            violations.add(
                    new Violation(
                            ViolationType.REQUIRED,
                            absent,
                            "Required option is not configured"
                    )
            );
        }
    }

    private void validateExactlyOne(
            ReadonlyConfig config,
            OptionRule.ExactlyOneRule rule,
            List<Violation> violations) {

        List<String> present =
                presentKeys(config, rule.options());

        if (present.size() != 1) {
            violations.add(
                    new Violation(
                            ViolationType.EXACTLY_ONE,
                            keys(rule.options()),
                            "Exactly one option must be configured; present="
                                    + present
                    )
            );
        }
    }

    private void validateAtMostOne(
            ReadonlyConfig config,
            OptionRule.AtMostOneRule rule,
            List<Violation> violations) {

        List<String> present =
                presentKeys(config, rule.options());

        if (present.size() > 1) {
            violations.add(
                    new Violation(
                            ViolationType.AT_MOST_ONE,
                            keys(rule.options()),
                            "At most one option may be configured; present="
                                    + present
                    )
            );
        }
    }

    private void validateAllOrNone(
            ReadonlyConfig config,
            OptionRule.AllOrNoneRule rule,
            List<Violation> violations) {

        List<String> present =
                presentKeys(config, rule.options());

        if (!present.isEmpty()
                && present.size() != rule.options().size()) {

            Set<String> absent =
                    new LinkedHashSet<String>(
                            keys(rule.options())
                    );

            absent.removeAll(present);

            violations.add(
                    new Violation(
                            ViolationType.ALL_OR_NONE,
                            keys(rule.options()),
                            "Options must be present or absent together; "
                                    + "present="
                                    + present
                                    + ", absent="
                                    + absent
                    )
            );
        }
    }

    private void validateConditional(
            ReadonlyConfig config,
            OptionRule.ConditionalRequiredRule rule,
            List<Violation> violations) {

        boolean matched;

        try {
            matched = rule.condition().matches(config);
        } catch (RuntimeException e) {
            violations.add(
                    new Violation(
                            ViolationType.CONDITIONAL_REQUIRED,
                            optionKeys(
                                    rule.condition()
                                            .referencedOptions()
                            ),
                            "Unable to evaluate condition: "
                                    + rule.condition().description()
                    )
            );
            return;
        }

        if (!matched) {
            return;
        }

        List<String> absent = new ArrayList<String>();

        for (Option<?> option : rule.requiredOptions()) {
            if (!isResolved(config, option)) {
                absent.add(option.key());
            }
        }

        if (!absent.isEmpty()) {
            violations.add(
                    new Violation(
                            ViolationType.CONDITIONAL_REQUIRED,
                            absent,
                            "Required when "
                                    + rule.condition().description()
                    )
            );
        }
    }

    private <T> void validateValue(
            ReadonlyConfig config,
            OptionRule.ValueRule<T> rule,
            List<Violation> violations,
            Set<String> typeErrorKeys) {

        Option<T> option = rule.option();

        if (typeErrorKeys.contains(option.key())
                || !isResolved(config, option)) {
            return;
        }

        T value = config.get(option);
        boolean valid;

        try {
            valid = rule.constraint().test(config, value);
        } catch (RuntimeException e) {
            valid = false;
        }

        if (!valid) {
            violations.add(
                    new Violation(
                            ViolationType.VALUE_CONSTRAINT,
                            Collections.singletonList(option.key()),
                            rule.constraint().description()
                    )
            );
        }
    }

    private boolean isResolved(
            ReadonlyConfig config,
            Option<?> option) {

        return config.contains(option)
                || option.hasDefaultValue();
    }

    private List<String> presentKeys(
            ReadonlyConfig config,
            List<Option<?>> options) {

        List<String> present = new ArrayList<String>();

        for (Option<?> option : options) {
            if (isResolved(config, option)) {
                present.add(option.key());
            }
        }

        return present;
    }

    private List<String> keys(
            List<Option<?>> options) {

        return optionKeys(options);
    }

    private List<String> optionKeys(
            Collection<Option<?>> options) {

        List<String> keys =
                new ArrayList<String>(options.size());

        for (Option<?> option : options) {
            keys.add(option.key());
        }

        return keys;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(
            Map<?, ?> map) {

        return (Map<String, Object>) map;
    }
}