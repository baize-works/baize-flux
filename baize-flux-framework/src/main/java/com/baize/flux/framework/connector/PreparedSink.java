package com.baize.flux.framework.connector;

import com.baize.flux.api.configuration.ReadonlyConfig;
import com.baize.flux.api.factory.SinkFactory;
import com.baize.flux.api.sink.SinkWriter;
import com.baize.flux.api.table.type.FluxRow;

import java.util.Objects;

/**
 * 已完成 Factory 发现和配置校验的 Sink。
 *
 * <p>每个 SinkTask 通过该对象创建独立 SinkWriter。
 */
public final class PreparedSink {

    private final String factoryIdentifier;

    private final SinkFactory factory;

    private final ReadonlyConfig options;

    public PreparedSink(
            String factoryIdentifier,
            SinkFactory factory,
            ReadonlyConfig options) {

        this.factoryIdentifier =
                Objects.requireNonNull(
                        factoryIdentifier,
                        "factoryIdentifier must not be null");

        this.factory =
                Objects.requireNonNull(
                        factory,
                        "factory must not be null");

        this.options =
                Objects.requireNonNull(
                        options,
                        "options must not be null");
    }

    public SinkWriter<FluxRow> createWriter() {
        SinkWriter<FluxRow> writer =
                factory.createSink(options);

        if (writer == null) {
            throw new ConnectorException(
                    "Sink factory '"
                            + factoryIdentifier
                            + "' returned a null writer");
        }

        return writer;
    }

    public String getFactoryIdentifier() {
        return factoryIdentifier;
    }

    public ReadonlyConfig getOptions() {
        return options;
    }
}