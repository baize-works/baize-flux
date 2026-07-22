package com.baize.flux.api.configuration;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/**
 * 配置规则条件。
 *
 * 用于判断条件配置规则是否生效，支持相等、存在、不存在、
 * 自定义条件以及与、或、非逻辑组合。
 *
 * @author weifuwan
 */
public final class RuleCondition {

    /**
     * 条件描述。
     */
    private final String description;

    /**
     * 条件判断逻辑。
     */
    private final Predicate<ReadonlyConfig> predicate;

    /**
     * 条件引用的配置项。
     */
    private final Set<Option<?>> referencedOptions;

    /**
     * 创建配置规则条件。
     *
     * @param description       条件描述
     * @param predicate         条件判断逻辑
     * @param referencedOptions 条件引用的配置项
     */
    private RuleCondition(
            String description,
            Predicate<ReadonlyConfig> predicate,
            Set<Option<?>> referencedOptions) {
        this.description =
                Objects.requireNonNull(
                        description,
                        "description"
                );
        this.predicate =
                Objects.requireNonNull(
                        predicate,
                        "predicate"
                );
        this.referencedOptions =
                Collections.unmodifiableSet(
                        new LinkedHashSet<>(
                                Objects.requireNonNull(
                                        referencedOptions,
                                        "referencedOptions"
                                )
                        )
                );
    }

    /**
     * 创建配置值等于指定值的条件。
     *
     * 条件判断会使用配置项的显式配置值或默认值。
     *
     * @param option        配置项
     * @param expectedValue 期望值
     * @param <T>           配置值类型
     * @return 配置规则条件
     */
    public static <T> RuleCondition equalTo(
            Option<T> option,
            T expectedValue) {
        Objects.requireNonNull(
                option,
                "option"
        );

        return new RuleCondition(
                "'"
                        + option.key()
                        + "' == "
                        + expectedValue,
                config ->
                        config.getResolvedOptional(option)
                                .map(
                                        value ->
                                                Objects.equals(
                                                        value,
                                                        expectedValue
                                                )
                                )
                                .orElse(false),
                Collections.<Option<?>>singleton(option)
        );
    }

    /**
     * 创建配置项已显式配置的条件。
     *
     * 配置项名称或备用配置项名称存在时，条件成立。
     * 配置项默认值不参与判断。
     *
     * @param option 配置项
     * @return 配置规则条件
     */
    public static RuleCondition present(
            Option<?> option) {
        Objects.requireNonNull(
                option,
                "option"
        );

        return new RuleCondition(
                "'"
                        + option.key()
                        + "' is configured",
                config -> config.contains(option),
                Collections.<Option<?>>singleton(option)
        );
    }

    /**
     * 创建配置项未显式配置的条件。
     *
     * 配置项名称及所有备用配置项名称均不存在时，条件成立。
     * 配置项默认值不参与判断。
     *
     * @param option 配置项
     * @return 配置规则条件
     */
    public static RuleCondition absent(
            Option<?> option) {
        Objects.requireNonNull(
                option,
                "option"
        );

        return new RuleCondition(
                "'"
                        + option.key()
                        + "' is not configured",
                config -> !config.contains(option),
                Collections.<Option<?>>singleton(option)
        );
    }

    /**
     * 创建自定义配置规则条件。
     *
     * @param description       条件描述
     * @param referencedOptions 条件引用的配置项
     * @param predicate         条件判断逻辑
     * @return 配置规则条件
     */
    public static RuleCondition custom(
            String description,
            Set<Option<?>> referencedOptions,
            Predicate<ReadonlyConfig> predicate) {
        return new RuleCondition(
                description,
                predicate,
                referencedOptions
        );
    }

    /**
     * 将当前条件与另一个条件进行逻辑与组合。
     *
     * 两个条件均成立时，组合条件成立。
     *
     * @param other 另一个配置规则条件
     * @return 组合后的配置规则条件
     */
    public RuleCondition and(
            RuleCondition other) {
        Objects.requireNonNull(
                other,
                "other"
        );

        Set<Option<?>> references =
                merge(
                        referencedOptions,
                        other.referencedOptions
                );

        return new RuleCondition(
                "("
                        + description
                        + " && "
                        + other.description
                        + ")",
                config ->
                        matches(config)
                                && other.matches(config),
                references
        );
    }

    /**
     * 将当前条件与另一个条件进行逻辑或组合。
     *
     * 任意一个条件成立时，组合条件成立。
     *
     * @param other 另一个配置规则条件
     * @return 组合后的配置规则条件
     */
    public RuleCondition or(
            RuleCondition other) {
        Objects.requireNonNull(
                other,
                "other"
        );

        Set<Option<?>> references =
                merge(
                        referencedOptions,
                        other.referencedOptions
                );

        return new RuleCondition(
                "("
                        + description
                        + " || "
                        + other.description
                        + ")",
                config ->
                        matches(config)
                                || other.matches(config),
                references
        );
    }

    /**
     * 对当前条件进行逻辑取反。
     *
     * @return 取反后的配置规则条件
     */
    public RuleCondition negate() {
        return new RuleCondition(
                "!("
                        + description
                        + ")",
                config -> !matches(config),
                referencedOptions
        );
    }

    /**
     * 判断指定配置是否满足当前条件。
     *
     * @param config 只读配置
     * @return 满足条件时返回 {@code true}
     */
    public boolean matches(
            ReadonlyConfig config) {
        return predicate.test(config);
    }

    /**
     * 获取条件描述。
     *
     * @return 条件描述
     */
    public String description() {
        return description;
    }

    /**
     * 获取条件引用的配置项。
     *
     * @return 不可修改的配置项集合
     */
    public Set<Option<?>> referencedOptions() {
        return referencedOptions;
    }

    /**
     * 合并两个配置项集合。
     *
     * 使用插入顺序去重，优先保留左侧集合中的配置项顺序。
     *
     * @param left  左侧配置项集合
     * @param right 右侧配置项集合
     * @return 合并后的配置项集合
     */
    private static Set<Option<?>> merge(
            Set<Option<?>> left,
            Set<Option<?>> right) {
        Set<Option<?>> result =
                new LinkedHashSet<>(left);

        result.addAll(right);
        return result;
    }
}
