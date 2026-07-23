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
 * 配置校验结果。
 * <p>
 * 用于聚合配置校验过程中产生的违规信息，
 * 可供命令行、REST API 和 Web UI 等调用方统一处理。
 *
 * @author weifuwan
 */
public final class ValidationResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 不包含任何违规信息的有效校验结果。
     */
    private static final ValidationResult VALID_RESULT =
            new ValidationResult(
                    Collections.emptyList()
            );

    /**
     * 配置违规信息。
     */
    private final List<Violation> violations;

    /**
     * 根据违规信息创建配置校验结果。
     * <p>
     * 传入的违规信息会被复制并转换为不可修改集合。
     *
     * @param violations 配置违规信息
     */
    public ValidationResult(
            List<Violation> violations) {
        Objects.requireNonNull(
                violations,
                "violations must not be null"
        );

        List<Violation> copiedViolations =
                new ArrayList<>(violations.size());

        for (Violation violation : violations) {
            copiedViolations.add(
                    Objects.requireNonNull(
                            violation,
                            "violation must not be null"
                    )
            );
        }

        this.violations =
                Collections.unmodifiableList(
                        copiedViolations
                );
    }

    /**
     * 获取不包含违规信息的有效校验结果。
     *
     * @return 有效校验结果
     */
    public static ValidationResult valid() {
        return VALID_RESULT;
    }

    /**
     * 创建包含一条违规信息的校验结果。
     *
     * @param violation 配置违规信息
     * @return 配置校验结果
     */
    public static ValidationResult of(
            Violation violation) {
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
     * 创建包含多条违规信息的校验结果。
     *
     * @param violations 配置违规信息
     * @return 配置校验结果
     */
    public static ValidationResult of(
            List<Violation> violations) {
        return new ValidationResult(violations);
    }

    /**
     * 创建配置校验结果构建器。
     *
     * @return 配置校验结果构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 规范化配置项名称集合。
     * <p>
     * 配置项名称会被去除首尾空白，并按照传入顺序去重。
     *
     * @param optionKeys 原始配置项名称
     * @return 不可修改的配置项名称集合
     */
    private static List<String> normalizeOptionKeys(
            List<String> optionKeys) {

        Objects.requireNonNull(
                optionKeys,
                "optionKeys must not be null"
        );

        Set<String> normalizedKeys =
                new LinkedHashSet<>();

        for (String optionKey : optionKeys) {
            normalizedKeys.add(
                    requireNonBlank(
                            optionKey,
                            "optionKey"
                    )
            );
        }

        return Collections.unmodifiableList(
                new ArrayList<>(normalizedKeys)
        );
    }

    /**
     * 校验并规范化非空白文本。
     *
     * @param value     待校验文本
     * @param fieldName 字段名称
     * @return 去除首尾空白后的文本
     */
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

    /**
     * 获取全部配置违规信息。
     *
     * @return 不可修改的违规信息集合
     */
    public List<Violation> violations() {
        return violations;
    }

    /**
     * 判断配置校验是否通过。
     *
     * @return 不存在违规信息时返回 {@code true}
     */
    public boolean isValid() {
        return violations.isEmpty();
    }

    /**
     * 判断配置校验是否未通过。
     *
     * @return 存在违规信息时返回 {@code true}
     */
    public boolean isInvalid() {
        return !isValid();
    }

    /**
     * 获取配置违规信息数量。
     *
     * @return 违规信息数量
     */
    public int violationCount() {
        return violations.size();
    }

    /**
     * 当配置校验未通过时抛出异常。
     *
     * @throws ConfigValidationException 存在配置违规信息时抛出
     */
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
     * 配置违规类型。
     */
    public enum ViolationType {

        /**
         * 存在未声明的配置项。
         */
        UNKNOWN_KEY,

        /**
         * 配置值无法转换为期望类型。
         */
        TYPE_MISMATCH,

        /**
         * 配置值不在允许值范围内。
         */
        ALLOWED_VALUES,

        /**
         * 缺少必填配置项。
         */
        REQUIRED,

        /**
         * 指定配置项中没有且仅有一个被配置。
         */
        EXACTLY_ONE,

        /**
         * 指定配置项中超过一个被配置。
         */
        AT_MOST_ONE,

        /**
         * 指定配置项未同时配置或同时省略。
         */
        ALL_OR_NONE,

        /**
         * 缺少条件成立时要求的配置项。
         */
        CONDITIONAL_REQUIRED,

        /**
         * 配置值不满足声明的值约束。
         */
        VALUE_CONSTRAINT
    }

    /**
     * 配置违规信息。
     * <p>
     * 描述一次配置校验失败的类型、关联配置项及异常明细。
     */
    public static final class Violation implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 配置违规类型。
         */
        private final ViolationType type;

        /**
         * 关联的配置项名称。
         */
        private final List<String> optionKeys;

        /**
         * 配置违规明细。
         */
        private final String message;

        /**
         * 创建配置违规信息。
         *
         * @param type       配置违规类型
         * @param optionKeys 关联的配置项名称
         * @param message    配置违规明细
         */
        public Violation(
                ViolationType type,
                List<String> optionKeys,
                String message) {

            this.type =
                    Objects.requireNonNull(
                            type,
                            "violation type must not be null"
                    );

            this.optionKeys =
                    normalizeOptionKeys(optionKeys);

            this.message =
                    requireNonBlank(
                            message,
                            "message"
                    );
        }

        /**
         * 创建关联单个配置项的违规信息。
         *
         * @param type      配置违规类型
         * @param optionKey 配置项名称
         * @param message   配置违规明细
         * @return 配置违规信息
         */
        public static Violation of(
                ViolationType type,
                String optionKey,
                String message) {

            return new Violation(
                    type,
                    Collections.singletonList(
                            requireNonBlank(
                                    optionKey,
                                    "optionKey"
                            )
                    ),
                    message
            );
        }

        /**
         * 创建关联多个配置项的违规信息。
         *
         * @param type       配置违规类型
         * @param optionKeys 配置项名称
         * @param message    配置违规明细
         * @return 配置违规信息
         */
        public static Violation of(
                ViolationType type,
                List<String> optionKeys,
                String message) {

            return new Violation(
                    type,
                    optionKeys,
                    message
            );
        }

        /**
         * 创建不关联具体配置项的全局违规信息。
         *
         * @param type    配置违规类型
         * @param message 配置违规明细
         * @return 全局配置违规信息
         */
        public static Violation global(
                ViolationType type,
                String message) {

            return new Violation(
                    type,
                    Collections.emptyList(),
                    message
            );
        }

        /**
         * 获取配置违规类型。
         *
         * @return 配置违规类型
         */
        public ViolationType type() {
            return type;
        }

        /**
         * 获取关联的配置项名称。
         *
         * @return 不可修改的配置项名称集合
         */
        public List<String> optionKeys() {
            return optionKeys;
        }

        /**
         * 获取配置违规明细。
         *
         * @return 配置违规明细
         */
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
                    && Objects.equals(
                    optionKeys,
                    that.optionKeys
            )
                    && Objects.equals(
                    message,
                    that.message
            );
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                    type,
                    optionKeys,
                    message
            );
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
     * 配置校验结果构建器。
     * <p>
     * 用于在配置校验过程中逐步收集违规信息。
     */
    public static final class Builder {

        /**
         * 已收集的配置违规信息。
         */
        private final List<Violation> violations =
                new ArrayList<>();

        /**
         * 创建配置校验结果构建器。
         */
        private Builder() {
        }

        /**
         * 添加配置违规信息。
         *
         * @param violation 配置违规信息
         * @return 当前构建器
         */
        public Builder add(
                Violation violation) {
            violations.add(
                    Objects.requireNonNull(
                            violation,
                            "violation must not be null"
                    )
            );

            return this;
        }

        /**
         * 添加关联单个配置项的违规信息。
         *
         * @param type      配置违规类型
         * @param optionKey 配置项名称
         * @param message   配置违规明细
         * @return 当前构建器
         */
        public Builder add(
                ViolationType type,
                String optionKey,
                String message) {

            return add(
                    Violation.of(
                            type,
                            optionKey,
                            message
                    )
            );
        }

        /**
         * 添加关联多个配置项的违规信息。
         *
         * @param type       配置违规类型
         * @param optionKeys 配置项名称
         * @param message    配置违规明细
         * @return 当前构建器
         */
        public Builder add(
                ViolationType type,
                List<String> optionKeys,
                String message) {

            return add(
                    Violation.of(
                            type,
                            optionKeys,
                            message
                    )
            );
        }

        /**
         * 添加全局配置违规信息。
         *
         * @param type    配置违规类型
         * @param message 配置违规明细
         * @return 当前构建器
         */
        public Builder addGlobal(
                ViolationType type,
                String message) {

            return add(
                    Violation.global(
                            type,
                            message
                    )
            );
        }

        /**
         * 批量添加配置违规信息。
         *
         * @param violations 配置违规信息
         * @return 当前构建器
         */
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

        /**
         * 判断当前是否未收集任何违规信息。
         *
         * @return 未收集违规信息时返回 {@code true}
         */
        public boolean isEmpty() {
            return violations.isEmpty();
        }

        /**
         * 获取已收集的违规信息数量。
         *
         * @return 违规信息数量
         */
        public int size() {
            return violations.size();
        }

        /**
         * 构建配置校验结果。
         * <p>
         * 未收集违规信息时返回共享的有效校验结果。
         *
         * @return 配置校验结果
         */
        public ValidationResult build() {
            if (violations.isEmpty()) {
                return ValidationResult.valid();
            }

            return new ValidationResult(violations);
        }
    }
}
