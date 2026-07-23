package com.baize.flux.api.factory;

import com.baize.flux.api.configuration.ReadonlyConfig;
import com.baize.flux.api.configuration.util.OptionRule;
import com.baize.flux.api.sink.SinkWriter;
import com.baize.flux.api.table.FluxRow;

public interface SinkFactory {
    String factoryIdentifier();

    OptionRule optionRule();

    SinkWriter<FluxRow> createSink(ReadonlyConfig config);
}
