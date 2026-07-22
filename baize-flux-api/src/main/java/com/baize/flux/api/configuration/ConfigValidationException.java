
        package com.baize.flux.api.configuration;

import com.baize.flux.common.exception.FluxException;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * 配置校验异常。
 *
 * 当配置内容未通过校验时抛出。
 * 异常中保留完整的校验结果，便于调用方获取具体的违规信息。
 *
 * @author weifuwan
 */
public class ConfigValidationException extends FluxException {

    private static final long serialVersionUID = 1L;

    /**
     * 配置校验结果。
     */
    private final ValidationResult validationResult;

    /**
     * 根据配置校验结果创建配置校验异常。
     *
     * @param validationResult 配置校验结果
     */
    public ConfigValidationException(
            ValidationResult validationResult) {
        this(validationResult, null);
    }

    /**
     * 根据配置校验结果和原始异常创建配置校验异常。
     *
     * @param validationResult 配置校验结果
     * @param cause            原始异常
     */
    public ConfigValidationException(
            ValidationResult validationResult,
            Throwable cause) {
        this(
                validate(validationResult),
                cause,
                true
        );
    }

    /**
     * 根据已校验的配置校验结果创建配置校验异常。
     *
     * @param validationResult 配置校验结果
     * @param cause            原始异常
     * @param validated        标识校验结果已经完成非空检查
     */
    private ConfigValidationException(
            ValidationResult validationResult,
            Throwable cause,
            boolean validated) {
        super(
                ConfigurationErrorCode.CONFIG_VALIDATION_FAILED,
                buildMessage(validationResult),
                cause,
                buildContext(validationResult)
        );
        this.validationResult = validationResult;
    }

    /**
     * 获取完整的配置校验结果。
     *
     * @return 配置校验结果
     */
    public ValidationResult validationResult() {
        return validationResult;
    }

    /**
     * 校验配置校验结果。
     *
     * @param validationResult 配置校验结果
     * @return 非空的配置校验结果
     */
    private static ValidationResult validate(
            ValidationResult validationResult) {
        return Objects.requireNonNull(
                validationResult,
                "validationResult must not be null"
        );
    }

    /**
     * 构建异常上下文。
     *
     * @param validationResult 配置校验结果
     * @return 异常上下文
     */
    private static Map<String, Object> buildContext(
            ValidationResult validationResult) {
        return Collections.singletonMap(
                "violationCount",
                validationResult.violations().size()
        );
    }

    /**
     * 构建异常明细。
     *
     * @param validationResult 配置校验结果
     * @return 异常明细
     */
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

