package com.baize.flux.framework.metrics;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Channel 运行指标。
 */
public final class ChannelMetrics {

    private final String channelId;

    private final AtomicLong enqueuedCount =
            new AtomicLong();

    private final AtomicLong dequeuedCount =
            new AtomicLong();

    private final AtomicInteger currentSize =
            new AtomicInteger();

    private final AtomicInteger maximumSize =
            new AtomicInteger();

    private final AtomicLong writeBlockedNanos =
            new AtomicLong();

    private final AtomicLong readBlockedNanos =
            new AtomicLong();

    private final long createdNanos = System.nanoTime();

    public ChannelMetrics(String channelId) {
        this.channelId =
                Objects.requireNonNull(
                        channelId,
                        "channelId must not be null");
    }

    public void recordEnqueued(int size) {
        enqueuedCount.incrementAndGet();
        currentSize.set(size);
        updateMaximumSize(size);
    }

    public void recordDequeued(int size) {
        dequeuedCount.incrementAndGet();
        currentSize.set(size);
    }

    public void addWriteBlockedNanos(long nanos) {
        if (nanos > 0) {
            writeBlockedNanos.addAndGet(nanos);
        }
    }

    public void addReadBlockedNanos(long nanos) {
        if (nanos > 0) {
            readBlockedNanos.addAndGet(nanos);
        }
    }

    private void updateMaximumSize(int size) {
        int currentMaximum;

        do {
            currentMaximum = maximumSize.get();

            if (size <= currentMaximum) {
                return;
            }
        } while (!maximumSize.compareAndSet(
                currentMaximum,
                size));
    }

    public String getChannelId() {
        return channelId;
    }

    public long getEnqueuedCount() {
        return enqueuedCount.get();
    }

    public long getDequeuedCount() {
        return dequeuedCount.get();
    }

    public int getCurrentSize() {
        return currentSize.get();
    }

    public int getMaximumSize() {
        return maximumSize.get();
    }

    public long getWriteBlockedMillis() {
        return writeBlockedNanos.get() / 1_000_000L;
    }

    public long getReadBlockedMillis() {
        return readBlockedNanos.get() / 1_000_000L;
    }

    /** Fraction of channel lifetime spent waiting on either side, capped at one. */
    public double getBlockedRatio() {
        long elapsed = Math.max(1L, System.nanoTime() - createdNanos);
        return Math.min(1D, (writeBlockedNanos.get() + readBlockedNanos.get()) / (double) elapsed);
    }
}
