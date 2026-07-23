package com.baize.flux.framework.configuration;

import com.baize.flux.api.configuration.ConfigurationErrorCode;
import com.baize.flux.common.exception.FluxException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * HOCON syntax or resolution failure.
 */
public class ConfigParseException extends FluxException {

    private static final long serialVersionUID = 1L;

    private final String source;
    private final Integer lineNumber;

    public ConfigParseException(
            String source, Integer lineNumber, String message, Throwable cause) {
        super(
                ConfigurationErrorCode.CONFIG_PARSE_FAILED,
                message,
                cause,
                buildContext(source, lineNumber)
        );
        this.source = source;
        this.lineNumber = lineNumber;
    }

    private static Map<String, Object> buildContext(
            String source,
            Integer lineNumber) {
        Map<String, Object> context = new LinkedHashMap<String, Object>();
        if (source != null) {
            context.put("source", source);
        }
        if (lineNumber != null) {
            context.put("lineNumber", lineNumber);
        }
        return context;
    }

    public String source() {
        return source;
    }

    public Integer lineNumber() {
        return lineNumber;
    }
}
