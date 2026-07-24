package com.baize.flux.framework.metrics;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/** Channel runtime metrics, including batch, record and byte backpressure accounting. */
public final class ChannelMetrics {
    private final String channelId;
    private final AtomicLong enqueuedCount = new AtomicLong();
    private final AtomicLong dequeuedCount = new AtomicLong();
    private final AtomicLong currentBatches = new AtomicLong();
    private final AtomicLong currentRecords = new AtomicLong();
    private final AtomicLong currentBytes = new AtomicLong();
    private final AtomicLong maximumBatches = new AtomicLong();
    private final AtomicLong maximumRecords = new AtomicLong();
    private final AtomicLong maximumBytes = new AtomicLong();
    private final AtomicLong writeBlockedNanos = new AtomicLong();
    private final AtomicLong readBlockedNanos = new AtomicLong();
    private final AtomicLong rateLimitWaitNanos = new AtomicLong();
    private final AtomicLong oversizedBatches = new AtomicLong();
    private final long createdNanos = System.nanoTime();

    public ChannelMetrics(String channelId) { this.channelId = Objects.requireNonNull(channelId, "channelId must not be null"); }
    public void recordEnqueued(long batches, long records, long bytes) {
        enqueuedCount.incrementAndGet(); currentBatches.set(batches); currentRecords.set(records); currentBytes.set(bytes);
        updateMaximum(maximumBatches, batches); updateMaximum(maximumRecords, records); updateMaximum(maximumBytes, bytes);
    }
    public void recordDequeued(long batches, long records, long bytes) {
        dequeuedCount.incrementAndGet(); currentBatches.set(batches); currentRecords.set(records); currentBytes.set(bytes);
    }
    /** Compatibility method for old channel implementations. */
    public void recordEnqueued(int size) { recordEnqueued(size, size, size); }
    /** Compatibility method for old channel implementations. */
    public void recordDequeued(int size) { recordDequeued(size, size, size); }
    public void recordOversizedBatch() { oversizedBatches.incrementAndGet(); }
    public void addWriteBlockedNanos(long nanos) { add(writeBlockedNanos, nanos); }
    public void addReadBlockedNanos(long nanos) { add(readBlockedNanos, nanos); }
    public void addRateLimitWaitNanos(long nanos) { add(rateLimitWaitNanos, nanos); }
    private static void add(AtomicLong target, long nanos) { if (nanos > 0) target.addAndGet(nanos); }
    private static void updateMaximum(AtomicLong maximum, long value) { long old; do { old = maximum.get(); if (value <= old) return; } while (!maximum.compareAndSet(old, value)); }
    public String getChannelId() { return channelId; }
    public long getEnqueuedCount() { return enqueuedCount.get(); }
    public long getDequeuedCount() { return dequeuedCount.get(); }
    public int getCurrentSize() { return (int) currentBatches.get(); }
    public int getMaximumSize() { return (int) maximumBatches.get(); }
    public long getCurrentBatches() { return currentBatches.get(); }
    public long getCurrentRecords() { return currentRecords.get(); }
    public long getCurrentBytes() { return currentBytes.get(); }
    public long getMaximumBatches() { return maximumBatches.get(); }
    public long getMaximumRecords() { return maximumRecords.get(); }
    public long getMaximumBytes() { return maximumBytes.get(); }
    public long getOversizedBatches() { return oversizedBatches.get(); }
    public long getWriteBlockedMillis() { return writeBlockedNanos.get() / 1_000_000L; }
    public long getReadBlockedMillis() { return readBlockedNanos.get() / 1_000_000L; }
    public long getRateLimitWaitMillis() { return rateLimitWaitNanos.get() / 1_000_000L; }
    public double getBlockedRatio() { long elapsed = Math.max(1L, System.nanoTime() - createdNanos); return Math.min(1D, (writeBlockedNanos.get() + readBlockedNanos.get()) / (double) elapsed); }
}
