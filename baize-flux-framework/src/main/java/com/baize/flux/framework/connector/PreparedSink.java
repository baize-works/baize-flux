package com.baize.flux.framework.connector;

import com.baize.flux.api.configuration.ReadonlyConfig;
import com.baize.flux.api.factory.SinkFactory;
import com.baize.flux.api.sink.SinkWriter;
import com.baize.flux.api.table.type.FluxRow;

import java.util.Objects;

/**
 * 已完成 Factory 发现、配置校验和 Writer 初始化的 Sink。
 *
 * <p>SinkWriter 在 Job Prepare 阶段创建，并在对应的 SinkTask 中执行。
 */
public final class PreparedSink {

    private final String factoryIdentifier;

    private final ReadonlyConfig options;

    private final SinkWriter<FluxRow> writer;

    public PreparedSink(
            String factoryIdentifier,
            SinkFactory factory,
            ReadonlyConfig options) {

        this.factoryIdentifier =
                Objects.requireNonNull(
                        factoryIdentifier,
                        "factoryIdentifier must not be null");

        this.options =
                Objects.requireNonNull(
                        options,
                        "options must not be null");

        SinkFactory nonNullFactory =
                Objects.requireNonNull(
                        factory,
                        "factory must not be null");

        this.writer =
                nonNullFactory.createSink(options);

        if (this.writer == null) {
            throw new ConnectorException(
                    "Sink factory '"
                            + factoryIdentifier
                            + "' returned a null writer");
        }
    }

    public SinkWriter<FluxRow> getWriter() {
        return writer;
    }

    public String getFactoryIdentifier() {
        return factoryIdentifier;
    }

    public ReadonlyConfig getOptions() {
        return options;
    }
}
