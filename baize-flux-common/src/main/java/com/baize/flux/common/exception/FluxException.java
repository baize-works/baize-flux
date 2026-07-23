package com.baize.flux.common.exception;

import lombok.Getter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Baize Flux 统一运行时异常。
 *
 * @author weifuwan
 */
@Getter
public class FluxException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码。
     */
    private final FluxErrorCode errorCode;

    /**
     * 错误详情。
     */
    private final String detail;

    /**
     * 错误上下文。
     */
    private final Map<String, Object> context;

    protected FluxException(
            FluxErrorCode errorCode,
            String detail,
            Throwable cause,
            Map<String, ?> context) {

        super(buildMessage(errorCode, detail, context), cause);

        this.errorCode = Objects.requireNonNull(
                errorCode,
                "errorCode must not be null"
        );
        this.detail = detail;
        this.context = immutableContext(context);
    }

    /**
     * 创建异常。
     */
    public static FluxException of(
            FluxErrorCode errorCode,
            String detail) {

        return of(errorCode, detail, Collections.emptyMap());
    }

    /**
     * 创建带上下文的异常。
     */
    public static FluxException of(
            FluxErrorCode errorCode,
            String detail,
            Map<String, ?> context) {

        return new FluxException(
                errorCode,
                detail,
                null,
                context
        );
    }

    /**
     * 包装底层异常。
     */
    public static FluxException wrap(
            FluxErrorCode errorCode,
            String detail,
            Throwable cause) {

        return wrap(
                errorCode,
                detail,
                cause,
                Collections.emptyMap()
        );
    }

    /**
     * 包装底层异常并携带上下文。
     */
    public static FluxException wrap(
            FluxErrorCode errorCode,
            String detail,
            Throwable cause,
            Map<String, ?> context) {

        return new FluxException(
                errorCode,
                detail,
                cause,
                context
        );
    }

    private static String buildMessage(
            FluxErrorCode errorCode,
            String detail,
            Map<String, ?> context) {

        Objects.requireNonNull(
                errorCode,
                "errorCode must not be null"
        );

        StringBuilder message = new StringBuilder(
                errorCode.format()
        );

        if (hasText(detail)) {
            message.append(", Detail:[")
                    .append(detail)
                    .append(']');
        }

        if (context != null && !context.isEmpty()) {
            message.append(", Context:")
                    .append(context);
        }

        return message.toString();
    }

    private static Map<String, Object> immutableContext(
            Map<String, ?> source) {

        if (source == null || source.isEmpty()) {
            return Collections.emptyMap();
        }

        return Collections.unmodifiableMap(
                new LinkedHashMap<String, Object>(source)
        );
    }

    private static String getCauseMessage(Throwable cause) {
        String message = cause.getMessage();

        return hasText(message)
                ? message
                : cause.getClass().getName();
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public String getCode() {
        return errorCode.getCode();
    }

    public ErrorCategory getCategory() {
        return errorCode.getCategory();
    }

    public String getDescription() {
        return errorCode.getDescription();
    }

    public boolean isRetryable() {
        return errorCode.isRetryable();
    }
}