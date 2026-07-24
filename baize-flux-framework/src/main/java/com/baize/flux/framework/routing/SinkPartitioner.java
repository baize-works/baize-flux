package com.baize.flux.framework.routing;

import com.baize.flux.framework.channel.RecordEnvelope;
import com.baize.flux.framework.job.SinkPartitionStrategy;

/**
 * Framework-owned stable sink router.
 */
public final class SinkPartitioner<T> implements Partitioner<RecordEnvelope<T>> {
    private final SinkPartitionStrategy strategy;

    public SinkPartitioner(SinkPartitionStrategy strategy) {
        this.strategy = strategy == null ? SinkPartitionStrategy.TABLE_AFFINITY : strategy;
    }

    @Override
    public int selectChannel(RecordEnvelope<T> value, int numberOfChannels) {
        if (numberOfChannels <= 0) throw new IllegalArgumentException("numberOfChannels must be greater than 0");
        int hash = value.getTablePath().hashCode();
        if (strategy == SinkPartitionStrategy.SPLIT_HASH) hash = 31 * hash + value.getBatch().getSplitId().hashCode();
        return Math.floorMod(hash, numberOfChannels);
    }
}
