package com.baize.flux.framework.planner;

import com.baize.flux.api.source.BoundedSource;
import com.baize.flux.api.sink.SinkWriter;
import com.baize.flux.api.table.FluxRow;

/**
 * Executable local physical plan.
 */
public final class PhysicalPlan {
    private final BoundedSource<FluxRow, ?> source;
    private final SinkWriter<FluxRow> sink;
    private final int channelCapacity;

    PhysicalPlan(BoundedSource<FluxRow, ?> source, SinkWriter<FluxRow> sink, int channelCapacity) {
        this.source = source;
        this.sink = sink;
        this.channelCapacity = channelCapacity;
    }

    public BoundedSource<FluxRow, ?> source() {
        return source;
    }

    public SinkWriter<FluxRow> sink() {
        return sink;
    }

    public int channelCapacity() {
        return channelCapacity;
    }
}
