package com.baize.flux.framework.configuration;

/** HOCON syntax or resolution failure. */
public class ConfigParseException extends RuntimeException {

    private final String source;
    private final Integer lineNumber;

    public ConfigParseException(
            String source, Integer lineNumber, String message, Throwable cause) {
        super(message, cause);
        this.source = source;
        this.lineNumber = lineNumber;
    }

    public String source() {
        return source;
    }

    public Integer lineNumber() {
        return lineNumber;
    }
}
