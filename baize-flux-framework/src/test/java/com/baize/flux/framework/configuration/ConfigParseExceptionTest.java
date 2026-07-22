package com.baize.flux.framework.configuration;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConfigParseExceptionTest {

    @Test
    public void usesUnifiedConfigurationErrorCodeAndContext() {
        ConfigParseException exception = new ConfigParseException(
                "application.conf", Integer.valueOf(12), "Invalid HOCON", null);

        assertEquals("CONFIG-004", exception.getCode());
        assertEquals("application.conf", exception.getContext().get("source"));
        assertEquals(Integer.valueOf(12), exception.getContext().get("lineNumber"));
    }
}
