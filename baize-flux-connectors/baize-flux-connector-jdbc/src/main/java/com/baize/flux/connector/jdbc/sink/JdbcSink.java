package com.baize.flux.connector.jdbc.sink;

import com.baize.flux.api.sink.Sink;
import com.baize.flux.api.sink.SinkWriter;
import com.baize.flux.api.sink.SinkWriterContext;
import com.baize.flux.api.table.type.FluxRow;
import com.baize.flux.connector.jdbc.config.JdbcSinkConfig;
import java.util.Objects;

/**
 * JDBC 的不可变 Job 级 Sink。
 *
 * <p>仅共享不可变配置；每次创建 Writer 都会创建独立的 OutputFormat、Connection 与 Statement 缓存。
 */
public final class JdbcSink implements Sink {
    private final JdbcSinkConfig config;
    public JdbcSink(JdbcSinkConfig config) { this.config = Objects.requireNonNull(config, "config must not be null"); }
    @Override
    public SinkWriter<FluxRow> createWriter(SinkWriterContext context) {
        Objects.requireNonNull(context, "context must not be null");
        return new JdbcSinkWriter(config, context.getClassLoader());
    }
}
