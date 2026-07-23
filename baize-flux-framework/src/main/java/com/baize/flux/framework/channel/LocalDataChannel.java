package com.baize.flux.framework.channel;

import com.baize.flux.framework.metrics.ChannelMetrics;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 基于内存的本地有界 Channel。
 *
 * <p>支持：
 *
 * <ul>
 *     <li>多个生产者</li>
 *     <li>单个消费者</li>
 *     <li>有界队列和背压</li>
 *     <li>生产者完成计数</li>
 *     <li>失败传播</li>
 *     <li>全局取消</li>
 * </ul>
 */
public final class LocalDataChannel<T>
        implements DataChannel<T> {

    private final String channelId;

    private final int capacity;

    private final int expectedProducers;

    private final Deque<T> buffer;

    private final ReentrantLock lock;

    private final Condition notEmpty;

    private final Condition notFull;

    private final ChannelMetrics metrics;

    private int createdWriters;

    private int remainingProducers;

    private boolean readerOpened;

    private boolean cancelled;

    private Throwable failure;

    public LocalDataChannel(
            String channelId,
            int capacity,
            int expectedProducers) {

        if (capacity <= 0) {
            throw new IllegalArgumentException(
                    "capacity must be greater than 0");
        }

        if (expectedProducers <= 0) {
            throw new IllegalArgumentException(
                    "expectedProducers must be greater than 0");
        }

        this.channelId =
                Objects.requireNonNull(
                        channelId,
                        "channelId must not be null");

        this.capacity = capacity;
        this.expectedProducers = expectedProducers;
        this.remainingProducers = expectedProducers;

        this.buffer = new ArrayDeque<T>(capacity);
        this.lock = new ReentrantLock();
        this.notEmpty = lock.newCondition();
        this.notFull = lock.newCondition();
        this.metrics = new ChannelMetrics(channelId);
    }

    @Override
    public ChannelWriter<T> openWriter() {
        lock.lock();

        try {
            ensureNotCancelledOrFailed();

            if (createdWriters >= expectedProducers) {
                throw new IllegalStateException(
                        "Too many writers created for channel '"
                                + channelId
                                + "', expected="
                                + expectedProducers);
            }

            createdWriters++;

            return new LocalChannelWriter();

        } finally {
            lock.unlock();
        }
    }

    @Override
    public ChannelReader<T> openReader() {
        lock.lock();

        try {
            if (readerOpened) {
                throw new IllegalStateException(
                        "Channel '" + channelId
                                + "' only supports one reader");
            }

            readerOpened = true;

            return new LocalChannelReader();

        } finally {
            lock.unlock();
        }
    }

    @Override
    public void fail(Throwable cause) {
        Objects.requireNonNull(
                cause,
                "cause must not be null");

        lock.lock();

        try {
            if (failure == null) {
                failure = cause;
            }

            notEmpty.signalAll();
            notFull.signalAll();

        } finally {
            lock.unlock();
        }
    }

    @Override
    public void cancel() {
        lock.lock();

        try {
            cancelled = true;

            notEmpty.signalAll();
            notFull.signalAll();

        } finally {
            lock.unlock();
        }
    }

    @Override
    public ChannelMetrics getMetrics() {
        return metrics;
    }

    @Override
    public void close() {
        cancel();
    }

    private void writeInternal(T value)
            throws Exception {

        Objects.requireNonNull(
                value,
                "channel value must not be null");

        lock.lockInterruptibly();

        try {
            while (buffer.size() >= capacity
                    && failure == null
                    && !cancelled) {

                long start = System.nanoTime();

                try {
                    notFull.await();
                } finally {
                    metrics.addWriteBlockedNanos(
                            System.nanoTime() - start);
                }
            }

            ensureNotCancelledOrFailed();

            buffer.addLast(value);

            metrics.recordEnqueued(buffer.size());

            notEmpty.signal();

        } finally {
            lock.unlock();
        }
    }

    private T readInternal()
            throws Exception {

        lock.lockInterruptibly();

        try {
            while (buffer.isEmpty()
                    && remainingProducers > 0
                    && failure == null
                    && !cancelled) {

                long start = System.nanoTime();

                try {
                    notEmpty.await();
                } finally {
                    metrics.addReadBlockedNanos(
                            System.nanoTime() - start);
                }
            }

            if (!buffer.isEmpty()) {
                T value = buffer.removeFirst();

                metrics.recordDequeued(buffer.size());

                notFull.signal();

                return value;
            }

            if (failure != null) {
                throw new IllegalStateException(
                        "Channel '" + channelId
                                + "' execution failed",
                        failure);
            }

            if (cancelled) {
                throw new CancellationException(
                        "Channel '" + channelId
                                + "' has been cancelled");
            }

            /*
             * 队列为空，并且所有生产者均已完成。
             */
            return null;

        } finally {
            lock.unlock();
        }
    }

    private void producerFinished() {
        lock.lock();

        try {
            if (remainingProducers > 0) {
                remainingProducers--;
            }

            if (remainingProducers == 0) {
                notEmpty.signalAll();
            }

        } finally {
            lock.unlock();
        }
    }

    private void ensureNotCancelledOrFailed() {
        if (failure != null) {
            throw new IllegalStateException(
                    "Channel '" + channelId
                            + "' execution failed",
                    failure);
        }

        if (cancelled) {
            throw new CancellationException(
                    "Channel '" + channelId
                            + "' has been cancelled");
        }
    }

    private final class LocalChannelWriter
            implements ChannelWriter<T> {

        private final AtomicBoolean closed =
                new AtomicBoolean(false);

        @Override
        public void write(T value)
                throws Exception {

            if (closed.get()) {
                throw new IllegalStateException(
                        "Channel writer has already been closed");
            }

            writeInternal(value);
        }

        @Override
        public void fail(Throwable cause) {
            LocalDataChannel.this.fail(cause);
        }

        @Override
        public void close() {
            if (closed.compareAndSet(false, true)) {
                producerFinished();
            }
        }
    }

    private final class LocalChannelReader
            implements ChannelReader<T> {

        @Override
        public T read() throws Exception {
            return readInternal();
        }
    }
}