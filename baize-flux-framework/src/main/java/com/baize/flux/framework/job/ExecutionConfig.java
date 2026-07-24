package com.baize.flux.framework.job;

/** Framework execution configuration. A non-positive optional limit is disabled. */
public final class ExecutionConfig {
    public static final int DEFAULT_BATCH_SIZE = 1_000;
    public static final int DEFAULT_SOURCE_PARALLELISM = 1;
    public static final int DEFAULT_SINK_PARALLELISM = 1;
    public static final int DEFAULT_CHANNEL_CAPACITY = 32;
    private final int batchSize, sourceParallelism, sinkParallelism, maxBufferedBatches;
    private final long maxBufferedRecords, maxBufferedBytes, maxRecordsPerSecond, maxBytesPerSecond;
    public ExecutionConfig(int batchSize, int sourceParallelism, int sinkParallelism, int channelCapacity) {
        this(batchSize, sourceParallelism, sinkParallelism, channelCapacity, 0, 0, 0, 0);
    }
    public ExecutionConfig(int batchSize, int sourceParallelism, int sinkParallelism, int maxBufferedBatches, long maxBufferedRecords, long maxBufferedBytes, long maxRecordsPerSecond, long maxBytesPerSecond) {
        if (batchSize <= 0 || sourceParallelism <= 0 || sinkParallelism <= 0) throw new IllegalArgumentException("batch size and parallelism must be greater than 0");
        if (maxBufferedBatches <= 0 && maxBufferedRecords <= 0 && maxBufferedBytes <= 0) throw new IllegalArgumentException("at least one buffer limit must be greater than 0");
        if (maxRecordsPerSecond < 0 || maxBytesPerSecond < 0) throw new IllegalArgumentException("rate limits must not be negative");
        this.batchSize=batchSize; this.sourceParallelism=sourceParallelism; this.sinkParallelism=sinkParallelism; this.maxBufferedBatches=maxBufferedBatches;
        this.maxBufferedRecords=maxBufferedRecords; this.maxBufferedBytes=maxBufferedBytes; this.maxRecordsPerSecond=maxRecordsPerSecond; this.maxBytesPerSecond=maxBytesPerSecond;
    }
    public int getBatchSize(){return batchSize;} public int getSourceParallelism(){return sourceParallelism;} public int getSinkParallelism(){return sinkParallelism;}
    /** Compatibility alias for max buffered batches. */ public int getChannelCapacity(){return maxBufferedBatches;}
    public int getMaxBufferedBatches(){return maxBufferedBatches;} public long getMaxBufferedRecords(){return maxBufferedRecords;} public long getMaxBufferedBytes(){return maxBufferedBytes;}
    public long getMaxRecordsPerSecond(){return maxRecordsPerSecond;} public long getMaxBytesPerSecond(){return maxBytesPerSecond;}
}
