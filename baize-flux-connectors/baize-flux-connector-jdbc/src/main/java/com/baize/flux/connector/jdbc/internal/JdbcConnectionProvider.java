package com.baize.flux.connector.jdbc.internal;

import com.baize.flux.connector.jdbc.config.JdbcConnectionConfig;
import com.baize.flux.connector.jdbc.core.dialect.JdbcDialect;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/** Creates connections with dialect defaults followed by user properties. */
public final class JdbcConnectionProvider {
    private final JdbcConnectionConfig config;
    private final JdbcDialect dialect;
    public JdbcConnectionProvider(JdbcConnectionConfig config, JdbcDialect dialect) { this.config = Objects.requireNonNull(config, "config must not be null"); this.dialect = Objects.requireNonNull(dialect, "dialect must not be null"); }
    public Connection getConnection() throws Exception {
        Class.forName(config.getDriverName());
        Properties properties = config.toProperties();
        for (Map.Entry<String, String> entry : dialect.resolveConnectionProperties(config.getProperties()).entrySet()) properties.setProperty(entry.getKey(), entry.getValue());
        return DriverManager.getConnection(config.getUrl(), properties);
    }
}
