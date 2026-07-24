package com.baize.flux.framework.job;

/**
 * Framework 执行配置。
 */
public final class ExecutionConfig {

    public static final int DEFAULT_BATCH_SIZE = 1_000;

    public static final int DEFAULT_SOURCE_PARALLELISM = 1;

    public static final int DEFAULT_SINK_PARALLELISM = 1;

    public static final int DEFAULT_CHANNEL_CAPACITY = 32;

    private final int batchSize;

    private final int sourceParallelism;

    private final int sinkParallelism;

    private final int channelCapacity;

    private final SinkPartitionStrategy sinkPartitionStrategy;

    private final SplitAssignmentMode splitAssignmentMode;

    public ExecutionConfig(
            int batchSize,
            int sourceParallelism,
            int sinkParallelism,
            int channelCapacity) {
        this(batchSize, sourceParallelism, sinkParallelism, channelCapacity, SinkPartitionStrategy.TABLE_AFFINITY, SplitAssignmentMode.STATIC_ROUND_ROBIN);
    }

    public ExecutionConfig(
            int batchSize, int sourceParallelism, int sinkParallelism, int channelCapacity,
            SinkPartitionStrategy sinkPartitionStrategy) {
        this(batchSize, sourceParallelism, sinkParallelism, channelCapacity, sinkPartitionStrategy, SplitAssignmentMode.STATIC_ROUND_ROBIN);
    }

    public ExecutionConfig(
            int batchSize, int sourceParallelism, int sinkParallelism, int channelCapacity,
            SinkPartitionStrategy sinkPartitionStrategy, SplitAssignmentMode splitAssignmentMode) {

        if (batchSize <= 0) { throw new IllegalArgumentException("batchSize must be greater than 0"); }
        if (sourceParallelism <= 0) { throw new IllegalArgumentException("sourceParallelism must be greater than 0"); }
        if (sinkParallelism <= 0) { throw new IllegalArgumentException("sinkParallelism must be greater than 0"); }
        if (channelCapacity <= 0) { throw new IllegalArgumentException("channelCapacity must be greater than 0"); }

        this.batchSize = batchSize;
        this.sourceParallelism = sourceParallelism;
        this.sinkParallelism = sinkParallelism;
        this.channelCapacity = channelCapacity;
        this.sinkPartitionStrategy = sinkPartitionStrategy == null ? SinkPartitionStrategy.TABLE_AFFINITY : sinkPartitionStrategy;
        this.splitAssignmentMode = splitAssignmentMode == null ? SplitAssignmentMode.STATIC_ROUND_ROBIN : splitAssignmentMode;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public int getSourceParallelism() {
        return sourceParallelism;
    }

    public int getSinkParallelism() {
        return sinkParallelism;
    }

    public int getChannelCapacity() {
        return channelCapacity;
    }

    public SplitAssignmentMode getSplitAssignmentMode() { return splitAssignmentMode; }

    public SinkPartitionStrategy getSinkPartitionStrategy() {
        return sinkPartitionStrategy;
    }
}