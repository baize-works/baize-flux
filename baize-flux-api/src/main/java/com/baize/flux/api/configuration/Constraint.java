package com.baize.flux.api.configuration;

import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * 配置值约束。
 *
 * 用于定义配置项的值级校验规则。
 *
 * @param <T> 配置值类型
 * @author weifuwan
 */
public final class Constraint<T> {

    /**
     * 约束规则描述。
     */
    private final String description;

    /**
     * 配置值校验逻辑。
     */
    private final BiPredicate<ReadonlyConfig, T> predicate;

    /**
     * 创建配置值约束。
     *
     * @param description 约束规则描述
     * @param predicate   配置值校验逻辑
     */
    private Constraint(
            String description,
            BiPredicate<ReadonlyConfig, T> predicate) {
        this.description =
                Objects.requireNonNull(description, "description");
        this.predicate =
                Objects.requireNonNull(predicate, "predicate");
    }

    /**
     * 根据规则描述和校验逻辑创建配置值约束。
     *
     * @param description 约束规则描述
     * @param predicate   配置值校验逻辑
     * @param <T>         配置值类型
     * @return 配置值约束
     */
    public static <T> Constraint<T> of(
            String description,
            BiPredicate<ReadonlyConfig, T> predicate) {
        return new Constraint<>(description, predicate);
    }

    /**
     * 校验配置值是否满足约束。
     *
     * @param config 配置内容
     * @param value  配置值
     * @return 满足约束时返回 {@code true}
     */
    public boolean test(
            ReadonlyConfig config,
            T value) {
        return predicate.test(config, value);
    }

    /**
     * 获取约束规则描述。
     *
     * @return 约束规则描述
     */
    public String description() {
        return description;
    }
}
