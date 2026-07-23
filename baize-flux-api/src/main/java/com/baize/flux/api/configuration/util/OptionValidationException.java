package com.baize.flux.api.configuration.util;


import com.baize.flux.api.configuration.Option;
import com.baize.flux.common.exception.FluxRuntimeException;
import com.baize.flux.common.exception.error.FluxAPIErrorCode;

/** Exception for all errors occurring during option validation phase. */
public class OptionValidationException extends FluxRuntimeException {

    private final String rawMessage;

    public OptionValidationException(String message, Throwable cause) {
        super(FluxAPIErrorCode.OPTION_VALIDATION_FAILED, message, cause);
        this.rawMessage = message;
    }

    public OptionValidationException(String message) {
        super(FluxAPIErrorCode.OPTION_VALIDATION_FAILED, message);
        this.rawMessage = message;
    }

    public OptionValidationException(String formatMessage, Object... args) {
        super(FluxAPIErrorCode.OPTION_VALIDATION_FAILED, String.format(formatMessage, args));
        this.rawMessage = String.format(formatMessage, args);
    }

    public OptionValidationException(Option<?> option) {
        super(
                FluxAPIErrorCode.OPTION_VALIDATION_FAILED,
                String.format(
                        "The option(\"%s\")  is incorrectly configured, please refer to the doc: %s",
                        option.key(), option.getDescription()));
        this.rawMessage =
                String.format(
                        "The option(\"%s\")  is incorrectly configured, please refer to the doc: %s",
                        option.key(), option.getDescription());
    }

    /**
     * Returns the raw validation message without the ErrorCode prefix. Use this instead of parsing
     * {@link #getMessage()} to avoid coupling to the error code format.
     */
    public String getRawMessage() {
        return rawMessage;
    }
}
