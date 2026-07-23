package com.baize.flux.api.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 只读配置。
 * <p>
 * 基于原始配置数据提供不可变、类型安全的配置访问能力。
 * 支持点分隔路径、备用配置项名称以及配置项默认值。
 *
 * @author weifuwan
 */
public final class ReadonlyConfig {

    /**
     * 不可修改的配置数据。
     */
    private final Map<String, Object> values;

    /**
     * 根据配置数据创建只读配置。
     * <p>
     * 配置数据将在构造时进行深度不可变处理。
     *
     * @param values 原始配置数据
     */
    private ReadonlyConfig(Map<String, Object> values) {
        this.values = deepImmutableMap(values);
    }

    /**
     * 根据 Map 创建只读配置。
     *
     * @param values 原始配置数据
     * @return 只读配置
     */
    public static ReadonlyConfig fromMap(
            Map<String, Object> values) {
        return new ReadonlyConfig(
                Objects.requireNonNull(
                        values,
                        "values"
                )
        );
    }

    /**
     * 创建深度不可修改的配置 Map。
     * <p>
     * Map 中嵌套的 Map 和 List 会被递归复制并转换为不可修改结构。
     *
     * @param source 原始 Map
     * @return 深度不可修改的配置 Map
     */
    private static Map<String, Object> deepImmutableMap(
            Map<?, ?> source) {
        Map<String, Object> result =
                new LinkedHashMap<>();

        source.forEach(
                (key, value) ->
                        result.put(
                                String.valueOf(key),
                                deepImmutableValue(value)
                        )
        );

        return Collections.unmodifiableMap(result);
    }

    /**
     * 将配置值转换为不可修改结构。
     * <p>
     * Map 和 List 将被递归复制并转换为不可修改结构，
     * 其他类型的值保持不变。
     *
     * @param value 原始配置值
     * @return 不可修改的配置值
     */
    private static Object deepImmutableValue(
            Object value) {
        if (value instanceof Map) {
            return deepImmutableMap(
                    (Map<?, ?>) value
            );
        }

        if (value instanceof List) {
            List<?> list = (List<?>) value;
            List<Object> result =
                    new ArrayList<>(list.size());

            list.forEach(
                    item ->
                            result.add(
                                    deepImmutableValue(item)
                            )
            );

            return Collections.unmodifiableList(result);
        }

        return value;
    }

    /**
     * 获取显式配置的配置值。
     * <p>
     * 按照配置项名称和备用配置项名称依次查找，但不使用默认值。
     *
     * @param option 配置项
     * @param <T>    配置值类型
     * @return 显式配置的配置值，不存在时返回空
     */
    public <T> Optional<T> getOptional(
            Option<T> option) {
        Objects.requireNonNull(
                option,
                "option"
        );

        Object rawValue = findValue(option.key());

        if (rawValue == null) {
            for (String fallbackKey : option.fallbackKeys()) {
                rawValue = findValue(fallbackKey);

                if (rawValue != null) {
                    break;
                }
            }
        }

        return rawValue == null
                ? Optional.empty()
                : Optional.of(
                option.convert(rawValue)
        );
    }

    /**
     * 获取解析后的配置值。
     * <p>
     * 优先返回显式配置的值；不存在时返回配置项声明的默认值。
     *
     * @param option 配置项
     * @param <T>    配置值类型
     * @return 显式配置值或默认值，均不存在时返回空
     */
    public <T> Optional<T> getResolvedOptional(
            Option<T> option) {
        Optional<T> configured = getOptional(option);

        if (configured.isPresent()) {
            return configured;
        }

        return option.defaultValueOptional();
    }

    /**
     * 获取配置值。
     * <p>
     * 优先返回显式配置的值，其次返回配置项默认值。
     * 当配置项不存在且未声明默认值时抛出异常。
     *
     * @param option 配置项
     * @param <T>    配置值类型
     * @return 配置值
     * @throws ConfigAccessException 配置项不存在且未声明默认值时抛出
     */
    public <T> T get(Option<T> option) {
        return getResolvedOptional(option)
                .orElseThrow(
                        () -> new ConfigAccessException(
                                option.key(),
                                "Option '"
                                        + option.key()
                                        + "' is not configured"
                        )
                );
    }

    /**
     * 判断配置项是否存在显式配置值。
     * <p>
     * 配置项名称或任意备用配置项名称存在时均返回 {@code true}，
     * 不考虑配置项默认值。
     *
     * @param option 配置项
     * @return 存在显式配置值时返回 {@code true}
     */
    public boolean contains(Option<?> option) {
        if (findValue(option.key()) != null) {
            return true;
        }

        return option.fallbackKeys()
                .stream()
                .anyMatch(
                        key -> findValue(key) != null
                );
    }

    /**
     * 判断指定配置项名称是否存在配置值。
     * <p>
     * 支持使用点分隔路径访问嵌套配置。
     *
     * @param key 配置项名称
     * @return 存在配置值时返回 {@code true}
     */
    public boolean contains(String key) {
        return findValue(key) != null;
    }

    /**
     * 获取未经类型转换的原始配置值。
     * <p>
     * 支持使用点分隔路径访问嵌套配置。
     *
     * @param key 配置项名称
     * @return 原始配置值，不存在时返回空
     */
    public Optional<Object> getRaw(String key) {
        return Optional.ofNullable(
                findValue(key)
        );
    }

    /**
     * 获取完整配置数据。
     * <p>
     * 返回的 Map 及其内部嵌套的 Map 和 List 均不可修改。
     *
     * @return 不可修改的配置数据
     */
    public Map<String, Object> asMap() {
        return values;
    }

    /**
     * 根据配置项名称查找原始配置值。
     * <p>
     * 优先查找顶层完整键名。当不存在完整键名时，
     * 再按照点分隔路径逐层查找嵌套配置。
     *
     * @param key 配置项名称
     * @return 原始配置值，不存在时返回 {@code null}
     */
    private Object findValue(String key) {
        if (values.containsKey(key)) {
            return values.get(key);
        }

        String[] parts = key.split("\\.");
        Object current = values;

        for (String part : parts) {
            if (!(current instanceof Map)) {
                return null;
            }

            current =
                    ((Map<?, ?>) current).get(part);

            if (current == null) {
                return null;
            }
        }

        return current;
    }
}
