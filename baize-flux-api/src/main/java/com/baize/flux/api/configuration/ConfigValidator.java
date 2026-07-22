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
 * 配置校验器。
 *
 * 用于校验未知配置项、配置值类型、配置结构规则以及配置值约束。
 *
 * @author weifuwan
 */
public final class ConfigValidator {

    /**
     * 是否拒绝未声明的配置项。
     */
    private final boolean rejectUnknownKeys;

    /**
     * 创建配置校验器。
     *
     * @param rejectUnknownKeys 是否拒绝未声明的配置项
     */
    private ConfigValidator(boolean rejectUnknownKeys) {
        this.rejectUnknownKeys = rejectUnknownKeys;
    }

    /**
     * 创建严格模式的配置校验器。
     *
     * 严格模式下，未声明的配置项将被视为校验错误。
     *
     * @return 配置校验器
     */
    public static ConfigValidator strict() {
        return new ConfigValidator(true);
    }

    /**
     * 创建宽松模式的配置校验器。
     *
     * 宽松模式下，不校验未声明的配置项。
     *
     * @return 配置校验器
     */
    public static ConfigValidator lenient() {
        return new ConfigValidator(false);
    }

    /**
     * 校验配置内容。
     *
     * @param config     待校验的只读配置
     * @param optionRule 配置项规则
     * @return 配置校验结果
     */
    public ValidationResult validate(
            ReadonlyConfig config,
            OptionRule optionRule) {

        List<Violation> violations = new ArrayList<>();
        Set<String> typeErrorKeys = new HashSet<>();

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

    /**
     * 校验配置中是否存在未声明的配置项。
     *
     * @param config     待校验的只读配置
     * @param optionRule 配置项规则
     * @param violations 违规信息集合
     */
    private void validateUnknownKeys(
            ReadonlyConfig config,
            OptionRule optionRule,
            List<Violation> violations) {

        Map<String, Option<?>> declared = new LinkedHashMap<>();

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

    /**
     * 递归收集未声明的配置项。
     *
     * @param values     当前层级的配置内容
     * @param prefix     当前配置路径前缀
     * @param declared   已声明的配置项
     * @param violations 违规信息集合
     */
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

    /**
     * 判断指定配置路径下是否存在已声明的子配置项。
     *
     * @param path         配置路径
     * @param declaredKeys 已声明的配置项名称
     * @return 存在子配置项时返回 {@code true}
     */
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

    /**
     * 查找允许包含当前配置路径的父配置项。
     *
     * @param path    配置路径
     * @param options 配置项集合
     * @return 匹配的父配置项，不存在时返回 {@code null}
     */
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

    /**
     * 校验配置值类型以及允许值范围。
     *
     * @param config        待校验的只读配置
     * @param optionRule    配置项规则
     * @param violations    违规信息集合
     * @param typeErrorKeys 类型转换失败的配置项名称
     */
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

    /**
     * 校验配置项之间的结构规则和配置值约束。
     *
     * @param config        待校验的只读配置
     * @param optionRule    配置项规则
     * @param violations    违规信息集合
     * @param typeErrorKeys 类型转换失败的配置项名称
     */
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

    /**
     * 校验必填配置项规则。
     *
     * @param config     待校验的只读配置
     * @param rule       必填配置项规则
     * @param violations 违规信息集合
     */
    private void validateRequired(
            ReadonlyConfig config,
            OptionRule.RequiredRule rule,
            List<Violation> violations) {

        List<String> absent = new ArrayList<>();

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

    /**
     * 校验配置项只能配置其中一个的规则。
     *
     * @param config     待校验的只读配置
     * @param rule       唯一配置项规则
     * @param violations 违规信息集合
     */
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

    /**
     * 校验配置项最多只能配置其中一个的规则。
     *
     * @param config     待校验的只读配置
     * @param rule       互斥配置项规则
     * @param violations 违规信息集合
     */
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

    /**
     * 校验配置项必须同时配置或同时不配置的规则。
     *
     * @param config     待校验的只读配置
     * @param rule       全有或全无规则
     * @param violations 违规信息集合
     */
    private void validateAllOrNone(
            ReadonlyConfig config,
            OptionRule.AllOrNoneRule rule,
            List<Violation> violations) {

        List<String> present =
                presentKeys(config, rule.options());

        if (!present.isEmpty()
                && present.size() != rule.options().size()) {

            Set<String> absent =
                    new LinkedHashSet<>(keys(rule.options()));

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

    /**
     * 校验条件必填规则。
     *
     * @param config     待校验的只读配置
     * @param rule       条件必填规则
     * @param violations 违规信息集合
     */
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

        List<String> absent = new ArrayList<>();

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

    /**
     * 校验配置值约束。
     *
     * @param config        待校验的只读配置
     * @param rule          配置值约束规则
     * @param violations    违规信息集合
     * @param typeErrorKeys 类型转换失败的配置项名称
     * @param <T>           配置值类型
     */
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

    /**
     * 判断配置项是否存在有效配置值。
     *
     * @param config 配置内容
     * @param option 配置项
     * @return 配置项已配置或存在默认值时返回 {@code true}
     */
    private boolean isResolved(
            ReadonlyConfig config,
            Option<?> option) {

        return config.contains(option)
                || option.hasDefaultValue();
    }

    /**
     * 获取已经配置的配置项名称。
     *
     * @param config  配置内容
     * @param options 配置项集合
     * @return 已配置的配置项名称
     */
    private List<String> presentKeys(
            ReadonlyConfig config,
            List<Option<?>> options) {

        List<String> present = new ArrayList<>();

        for (Option<?> option : options) {
            if (isResolved(config, option)) {
                present.add(option.key());
            }
        }

        return present;
    }

    /**
     * 获取配置项名称。
     *
     * @param options 配置项集合
     * @return 配置项名称
     */
    private List<String> keys(
            List<Option<?>> options) {

        return optionKeys(options);
    }

    /**
     * 获取配置项名称。
     *
     * @param options 配置项集合
     * @return 配置项名称
     */
    private List<String> optionKeys(
            Collection<Option<?>> options) {

        List<String> keys = new ArrayList<>(options.size());

        for (Option<?> option : options) {
            keys.add(option.key());
        }

        return keys;
    }

    /**
     * 将通用 Map 转换为配置 Map。
     *
     * @param map 原始 Map
     * @return 配置 Map
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(
            Map<?, ?> map) {

        return (Map<String, Object>) map;
    }
}
