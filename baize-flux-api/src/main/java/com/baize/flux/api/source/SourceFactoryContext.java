package com.baize.flux.api.source;

import com.baize.flux.api.configuration.ReadonlyConfig;

import java.util.Objects;

/**
 * Source 工厂上下文。
 */
public final class SourceFactoryContext {

    private final ReadonlyConfig options;

    public SourceFactoryContext(ReadonlyConfig options) {
        this.options = Objects.requireNonNull(
                options,
                "options must not be null");
    }

    public ReadonlyConfig getOptions() {
        return options;
    }
}
