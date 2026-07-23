package com.baize.flux.connector.jdbc.sink;

import com.baize.flux.connector.jdbc.config.JdbcSinkConfig;
import java.util.Objects;

/** Builder for the JDBC output format. */
public final class JdbcOutputFormatBuilder {
    private JdbcSinkConfig config;
    public JdbcOutputFormatBuilder withConfig(JdbcSinkConfig config) { this.config = Objects.requireNonNull(config, "config must not be null"); return this; }
    public JdbcOutputFormat build() { if (config == null) throw new IllegalStateException("JdbcSinkConfig must be configured before build"); return new JdbcOutputFormat(config); }
}
