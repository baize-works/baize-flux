package com.baize.flux.api.configuration;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 通用配置值约束工具类。
 *
 * 提供常见的配置值校验规则。
 * 连接器或业务模块的自定义校验规则可以通过 {@link Constraint#of} 创建。
 *
 * @author weifuwan
 */
public final class Constraints {

    /**
     * 工具类不允许实例化。
     */
    private Constraints() {
    }

    /**
     * 创建字符串非空白约束。
     *
     * @return 字符串非空白约束
     */
    public static Constraint<String> notBlank() {
        return Constraint.of(
                "must not be blank",
                (config, value) ->
                        value != null
                                && !value.trim().isEmpty()
        );
    }

    /**
     * 创建正则表达式匹配约束。
     *
     * @param regex 正则表达式
     * @return 正则表达式匹配约束
     */
    public static Constraint<String> matches(String regex) {
        Pattern pattern = Pattern.compile(regex);

        return Constraint.of(
                "must match pattern " + regex,
                (config, value) ->
                        value != null
                                && pattern.matcher(value).matches()
        );
    }

    /**
     * 创建大于或等于指定最小值的约束。
     *
     * @param minimum 最小值
     * @param <T>     配置值类型
     * @return 最小值约束
     */
    public static <T extends Comparable<T>>
    Constraint<T> greaterOrEqual(T minimum) {
        return Constraint.of(
                "must be greater than or equal to " + minimum,
                (config, value) ->
                        value != null
                                && value.compareTo(minimum) >= 0
        );
    }

    /**
     * 创建小于或等于指定最大值的约束。
     *
     * @param maximum 最大值
     * @param <T>     配置值类型
     * @return 最大值约束
     */
    public static <T extends Comparable<T>>
    Constraint<T> lessOrEqual(T maximum) {
        return Constraint.of(
                "must be less than or equal to " + maximum,
                (config, value) ->
                        value != null
                                && value.compareTo(maximum) <= 0
        );
    }

    /**
     * 创建指定范围内的配置值约束。
     *
     * 最小值和最大值均包含在有效范围内。
     *
     * @param minimum 最小值
     * @param maximum 最大值
     * @param <T>     配置值类型
     * @return 配置值范围约束
     */
    public static <T extends Comparable<T>>
    Constraint<T> between(
            T minimum,
            T maximum) {
        return Constraint.of(
                "must be between "
                        + minimum
                        + " and "
                        + maximum,
                (config, value) ->
                        value != null
                                && value.compareTo(minimum) >= 0
                                && value.compareTo(maximum) <= 0
        );
    }

    /**
     * 创建集合非空约束。
     *
     * @param <T> 集合类型
     * @return 集合非空约束
     */
    public static <T extends Collection<?>>
    Constraint<T> notEmptyCollection() {
        return Constraint.of(
                "must not be empty",
                (config, value) ->
                        value != null
                                && !value.isEmpty()
        );
    }

    /**
     * 创建集合元素唯一约束。
     *
     * @param <T> 集合类型
     * @return 集合元素唯一约束
     */
    public static <T extends Collection<?>>
    Constraint<T> uniqueCollection() {
        return Constraint.of(
                "must contain unique values",
                (config, value) ->
                        value != null
                                && value.size()
                                == new HashSet<>(value).size()
        );
    }

    /**
     * 创建 Map 非空约束。
     *
     * @param <T> Map 类型
     * @return Map 非空约束
     */
    public static <T extends Map<?, ?>>
    Constraint<T> notEmptyMap() {
        return Constraint.of(
                "must not be empty",
                (config, value) ->
                        value != null
                                && !value.isEmpty()
        );
    }
}
