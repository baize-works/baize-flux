package com.baize.flux.api.factory;

import com.baize.flux.api.configuration.ReadonlyConfig;
import com.baize.flux.api.configuration.util.OptionRule;
import com.baize.flux.api.sink.PreparedSinkMetadata;
import com.baize.flux.api.sink.SinkPreparer;
import com.baize.flux.api.sink.SinkWriter;
import com.baize.flux.api.table.type.FluxRow;

public interface SinkFactory extends Factory {

    OptionRule optionRule();

    /**
     * Creates the runtime writer only after preparation has completed.
     */
    default SinkWriter<FluxRow> createSink(ReadonlyConfig config, PreparedSinkMetadata metadata) {
        return createSink(config);
    }

    /**
     * @deprecated implement the metadata-aware overload for prepared sinks.
     */
    @Deprecated
    default SinkWriter<FluxRow> createSink(ReadonlyConfig config) {
        throw new UnsupportedOperationException("Sink factory must implement createSink(config, metadata)");
    }

    /**
     * Returns the preparation contract. The default preserves legacy sinks.
     */
    default SinkPreparer createPreparer(ReadonlyConfig config) {
        return context -> new PreparedSinkMetadata(context.getSourceTables());
    }
}
