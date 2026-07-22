package com.baize.flux.api.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 配置项规则。
 *
 * 用于声明支持的配置项，以及配置项之间的结构规则和配置值约束。
 *
 * @author weifuwan
 */
public final class OptionRule {

    /**
     * 配置规则标记接口。
     */
    public interface Rule {
    }

    /**
     * 必填配置项规则。
     *
     * 表示指定的所有配置项都必须存在有效配置值。
     */
    public static final class RequiredRule implements Rule {

        /**
         * 必填配置项。
         */
        private final List<Option<?>> options;

        /**
         * 创建必填配置项规则。
         *
         * @param options 必填配置项
         */
        public RequiredRule(List<Option<?>> options) {
            this.options = immutableOptions(options, "options");
        }

        /**
         * 获取必填配置项。
         *
         * @return 不可修改的配置项集合
         */
        public List<Option<?>> options() {
            return options;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (!(obj instanceof RequiredRule)) {
                return false;
            }

            RequiredRule that = (RequiredRule) obj;
            return Objects.equals(options, that.options);
        }

        @Override
        public int hashCode() {
            return Objects.hash(options);
        }

        @Override
        public String toString() {
            return "RequiredRule{"
                    + "options="
                    + options
                    + '}';
        }
    }

    /**
     * 唯一配置项规则。
     *
     * 表示指定配置项中必须有且仅有一个配置项存在有效配置值。
     */
    public static final class ExactlyOneRule implements Rule {

        /**
         * 参与校验的配置项。
         */
        private final List<Option<?>> options;

        /**
         * 创建唯一配置项规则。
         *
         * @param options 参与校验的配置项
         */
        public ExactlyOneRule(List<Option<?>> options) {
            this.options = immutableOptions(options, "options");
        }

        /**
         * 获取参与校验的配置项。
         *
         * @return 不可修改的配置项集合
         */
        public List<Option<?>> options() {
            return options;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (!(obj instanceof ExactlyOneRule)) {
                return false;
            }

            ExactlyOneRule that = (ExactlyOneRule) obj;
            return Objects.equals(options, that.options);
        }

        @Override
        public int hashCode() {
            return Objects.hash(options);
        }

        @Override
        public String toString() {
            return "ExactlyOneRule{"
                    + "options="
                    + options
                    + '}';
        }
    }

    /**
     * 最多一个配置项规则。
     *
     * 表示指定配置项中最多只能有一个配置项存在有效配置值。
     */
    public static final class AtMostOneRule implements Rule {

        /**
         * 参与校验的配置项。
         */
        private final List<Option<?>> options;

        /**
         * 创建最多一个配置项规则。
         *
         * @param options 参与校验的配置项
         */
        public AtMostOneRule(List<Option<?>> options) {
            this.options = immutableOptions(options, "options");
        }

        /**
         * 获取参与校验的配置项。
         *
         * @return 不可修改的配置项集合
         */
        public List<Option<?>> options() {
            return options;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (!(obj instanceof AtMostOneRule)) {
                return false;
            }

            AtMostOneRule that = (AtMostOneRule) obj;
            return Objects.equals(options, that.options);
        }

        @Override
        public int hashCode() {
            return Objects.hash(options);
        }

        @Override
        public String toString() {
            return "AtMostOneRule{"
                    + "options="
                    + options
                    + '}';
        }
    }

    /**
     * 全有或全无配置项规则。
     *
     * 表示指定配置项必须同时存在有效配置值，或者全部不配置。
     */
    public static final class AllOrNoneRule implements Rule {

        /**
         * 参与校验的配置项。
         */
        private final List<Option<?>> options;

        /**
         * 创建全有或全无配置项规则。
         *
         * @param options 参与校验的配置项
         */
        public AllOrNoneRule(List<Option<?>> options) {
            this.options = immutableOptions(options, "options");
        }

        /**
         * 获取参与校验的配置项。
         *
         * @return 不可修改的配置项集合
         */
        public List<Option<?>> options() {
            return options;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (!(obj instanceof AllOrNoneRule)) {
                return false;
            }

            AllOrNoneRule that = (AllOrNoneRule) obj;
            return Objects.equals(options, that.options);
        }

        @Override
        public int hashCode() {
            return Objects.hash(options);
        }

        @Override
        public String toString() {
            return "AllOrNoneRule{"
                    + "options="
                    + options
                    + '}';
        }
    }

    /**
     * 条件必填配置项规则。
     *
     * 当指定条件成立时，要求相关配置项必须存在有效配置值。
     */
    public static final class ConditionalRequiredRule implements Rule {

        /**
         * 规则生效条件。
         */
        private final RuleCondition condition;

        /**
         * 条件成立时的必填配置项。
         */
        private final List<Option<?>> requiredOptions;

        /**
         * 创建条件必填配置项规则。
         *
         * @param condition       规则生效条件
         * @param requiredOptions 条件成立时的必填配置项
         */
        public ConditionalRequiredRule(
                RuleCondition condition,
                List<Option<?>> requiredOptions) {

            this.condition =
                    Objects.requireNonNull(
                            condition,
                            "condition must not be null"
                    );

            this.requiredOptions =
                    immutableOptions(
                            requiredOptions,
                            "requiredOptions"
                    );
        }

        /**
         * 获取规则生效条件。
         *
         * @return 规则生效条件
         */
        public RuleCondition condition() {
            return condition;
        }

        /**
         * 获取条件成立时的必填配置项。
         *
         * @return 不可修改的必填配置项集合
         */
        public List<Option<?>> requiredOptions() {
            return requiredOptions;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (!(obj instanceof ConditionalRequiredRule)) {
                return false;
            }

            ConditionalRequiredRule that =
                    (ConditionalRequiredRule) obj;

            return Objects.equals(condition, that.condition)
                    && Objects.equals(
                    requiredOptions,
                    that.requiredOptions
            );
        }

        @Override
        public int hashCode() {
            return Objects.hash(condition, requiredOptions);
        }

        @Override
        public String toString() {
            return "ConditionalRequiredRule{"
                    + "condition="
                    + condition
                    + ", requiredOptions="
                    + requiredOptions
                    + '}';
        }
    }

    /**
     * 配置值约束规则。
     *
     * 用于声明单个配置项需要满足的值级约束。
     *
     * @param <T> 配置值类型
     */
    public static final class ValueRule<T> implements Rule {

        /**
         * 需要校验的配置项。
         */
        private final Option<T> option;

        /**
         * 配置值约束。
         */
        private final Constraint<T> constraint;

        /**
         * 创建配置值约束规则。
         *
         * @param option     需要校验的配置项
         * @param constraint 配置值约束
         */
        public ValueRule(
                Option<T> option,
                Constraint<T> constraint) {

            this.option =
                    Objects.requireNonNull(
                            option,
                            "option must not be null"
                    );

            this.constraint =
                    Objects.requireNonNull(
                            constraint,
                            "constraint must not be null"
                    );
        }

        /**
         * 获取需要校验的配置项。
         *
         * @return 配置项
         */
        public Option<T> option() {
            return option;
        }

        /**
         * 获取配置值约束。
         *
         * @return 配置值约束
         */
        public Constraint<T> constraint() {
            return constraint;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (!(obj instanceof ValueRule)) {
                return false;
            }

            ValueRule<?> that = (ValueRule<?>) obj;

            return Objects.equals(option, that.option)
                    && Objects.equals(
                    constraint,
                    that.constraint
            );
        }

        @Override
        public int hashCode() {
            return Objects.hash(option, constraint);
        }

        @Override
        public String toString() {
            return "ValueRule{"
                    + "option="
                    + option
                    + ", constraint="
                    + constraint
                    + '}';
        }
    }

    /**
     * 支持的配置项。
     */
    private final List<Option<?>> options;

    /**
     * 配置校验规则。
     */
    private final List<Rule> rules;

    /**
     * 创建配置项规则。
     *
     * @param options 支持的配置项
     * @param rules   配置校验规则
     */
    private OptionRule(
            List<Option<?>> options,
            List<Rule> rules) {

        this.options =
                immutableOptions(
                        options,
                        "options"
                );
        this.rules = immutableRules(rules);
    }

    /**
     * 创建配置项规则构建器。
     *
     * @return 配置项规则构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 获取支持的配置项。
     *
     * @return 不可修改的配置项集合
     */
    public List<Option<?>> options() {
        return options;
    }

    /**
     * 获取配置校验规则。
     *
     * @return 不可修改的配置规则集合
     */
    public List<Rule> rules() {
        return rules;
    }

    @Override
    public String toString() {
        return "OptionRule{"
                + "options="
                + options
                + ", rules="
                + rules
                + '}';
    }

    /**
     * 配置项规则构建器。
     *
     * 用于声明可选配置项、必填配置项、配置项结构关系以及配置值约束。
     */
    public static final class Builder {

        /**
         * 已注册的配置项。
         */
        private final Map<String, Option<?>> options =
                new LinkedHashMap<>();

        /**
         * 已声明的配置规则。
         */
        private final List<Rule> rules =
                new ArrayList<>();

        /**
         * 创建配置项规则构建器。
         */
        private Builder() {
        }

        /**
         * 注册可选配置项。
         *
         * @param options 可选配置项
         * @return 当前构建器
         */
        public Builder optional(Option<?>... options) {
            requireAtLeast(
                    1,
                    "optional",
                    options
            );
            register(options);
            return this;
        }

        /**
         * 注册必填配置项。
         *
         * 必填配置项不允许声明默认值。
         *
         * @param options 必填配置项
         * @return 当前构建器
         */
        public Builder required(Option<?>... options) {
            requireAtLeast(
                    1,
                    "required",
                    options
            );

            for (Option<?> option : options) {
                Objects.requireNonNull(
                        option,
                        "option must not be null"
                );

                if (option.hasDefaultValue()) {
                    throw new IllegalArgumentException(
                            "Required option '"
                                    + option.key()
                                    + "' must not define a default value"
                    );
                }
            }

            register(options);
            rules.add(
                    new RequiredRule(
                            Arrays.asList(options)
                    )
            );

            return this;
        }

        /**
         * 声明配置项中必须有且仅有一个存在有效配置值。
         *
         * @param options 参与校验的配置项
         * @return 当前构建器
         */
        public Builder exactlyOneOf(Option<?>... options) {
            requireAtLeast(
                    2,
                    "exactlyOneOf",
                    options
            );

            register(options);
            rules.add(
                    new ExactlyOneRule(
                            Arrays.asList(options)
                    )
            );

            return this;
        }

        /**
         * 声明配置项中最多只能有一个存在有效配置值。
         *
         * @param options 参与校验的配置项
         * @return 当前构建器
         */
        public Builder atMostOneOf(Option<?>... options) {
            requireAtLeast(
                    2,
                    "atMostOneOf",
                    options
            );

            register(options);
            rules.add(
                    new AtMostOneRule(
                            Arrays.asList(options)
                    )
            );

            return this;
        }

        /**
         * 声明配置项必须同时存在有效配置值或者全部不配置。
         *
         * @param options 参与校验的配置项
         * @return 当前构建器
         */
        public Builder allOrNone(Option<?>... options) {
            requireAtLeast(
                    2,
                    "allOrNone",
                    options
            );

            register(options);
            rules.add(
                    new AllOrNoneRule(
                            Arrays.asList(options)
                    )
            );

            return this;
        }

        /**
         * 声明条件必填配置项。
         *
         * 当指定条件成立时，相关配置项必须存在有效配置值。
         *
         * @param condition       规则生效条件
         * @param requiredOptions 条件成立时的必填配置项
         * @return 当前构建器
         */
        public Builder conditionalRequired(
                RuleCondition condition,
                Option<?>... requiredOptions) {

            Objects.requireNonNull(
                    condition,
                    "condition must not be null"
            );

            requireAtLeast(
                    1,
                    "conditionalRequired",
                    requiredOptions
            );

            Set<Option<?>> referencedOptions =
                    Objects.requireNonNull(
                            condition.referencedOptions(),
                            "condition referenced options must not be null"
                    );

            register(
                    referencedOptions.toArray(
                            new Option<?>[referencedOptions.size()]
                    )
            );

            register(requiredOptions);

            rules.add(
                    new ConditionalRequiredRule(
                            condition,
                            Arrays.asList(requiredOptions)
                    )
            );

            return this;
        }

        /**
         * 为配置项添加配置值约束。
         *
         * @param option     需要校验的配置项
         * @param constraint 配置值约束
         * @param <T>        配置值类型
         * @return 当前构建器
         */
        public <T> Builder constrain(
                Option<T> option,
                Constraint<T> constraint) {

            Objects.requireNonNull(
                    option,
                    "option must not be null"
            );

            Objects.requireNonNull(
                    constraint,
                    "constraint must not be null"
            );

            register(option);

            rules.add(
                    new ValueRule<>(
                            option,
                            constraint
                    )
            );

            return this;
        }

        /**
         * 构建配置项规则。
         *
         * @return 配置项规则
         */
        public OptionRule build() {
            return new OptionRule(
                    new ArrayList<>(options.values()),
                    new ArrayList<>(rules)
            );
        }

        /**
         * 注册配置项。
         *
         * 相同配置项名称只能由同一个 {@link Option} 实例声明。
         *
         * @param newOptions 待注册的配置项
         */
        private void register(Option<?>... newOptions) {
            Objects.requireNonNull(
                    newOptions,
                    "options must not be null"
            );

            for (Option<?> option : newOptions) {
                Objects.requireNonNull(
                        option,
                        "option must not be null"
                );

                Option<?> existing =
                        options.get(option.key());

                if (existing == null) {
                    options.put(option.key(), option);
                    continue;
                }

                if (existing != option) {
                    throw new IllegalArgumentException(
                            "Option key '"
                                    + option.key()
                                    + "' is declared by different Option instances"
                    );
                }
            }
        }

        /**
         * 校验配置项数量是否满足最低要求。
         *
         * @param minimum 最少配置项数量
         * @param method  调用方法名称
         * @param options 配置项
         */
        private static void requireAtLeast(
                int minimum,
                String method,
                Option<?>[] options) {

            if (options == null
                    || options.length < minimum) {
                throw new IllegalArgumentException(
                        method
                                + " requires at least "
                                + minimum
                                + " option(s)"
                );
            }
        }
    }

    /**
     * 创建不可修改的配置项集合。
     *
     * @param options   原始配置项集合
     * @param fieldName 字段名称
     * @return 不可修改的配置项集合
     */
    private static List<Option<?>> immutableOptions(
            List<Option<?>> options,
            String fieldName) {

        Objects.requireNonNull(
                options,
                fieldName + " must not be null"
        );

        List<Option<?>> copiedOptions =
                new ArrayList<>(options.size());

        for (Option<?> option : options) {
            copiedOptions.add(
                    Objects.requireNonNull(
                            option,
                            fieldName + " must not contain null"
                    )
            );
        }

        return Collections.unmodifiableList(copiedOptions);
    }

    /**
     * 创建不可修改的配置规则集合。
     *
     * @param rules 原始配置规则集合
     * @return 不可修改的配置规则集合
     */
    private static List<Rule> immutableRules(
            List<Rule> rules) {

        Objects.requireNonNull(
                rules,
                "rules must not be null"
        );

        List<Rule> copiedRules =
                new ArrayList<>(rules.size());

        for (Rule rule : rules) {
            copiedRules.add(
                    Objects.requireNonNull(
                            rule,
                            "rules must not contain null"
                    )
            );
        }

        return Collections.unmodifiableList(copiedRules);
    }
}
