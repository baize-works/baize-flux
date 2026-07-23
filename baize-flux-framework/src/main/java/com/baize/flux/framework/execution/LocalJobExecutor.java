package com.baize.flux.framework.execution;

import com.baize.flux.api.source.*;
import com.baize.flux.api.sink.SinkWriter;
import com.baize.flux.api.table.*;
import com.baize.flux.framework.channel.*;
import com.baize.flux.framework.planner.PhysicalPlan;

import java.util.*;
import java.util.concurrent.atomic.*;

/**
 * Two-thread bounded executor with backpressure and coordinated failure rollback.
 */
public final class LocalJobExecutor {
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void read(BoundedSource source, Channel<RecordBatch<FluxRow>> channel, AtomicLong count) throws Exception {
        List splits = source.planSplits(1);
        if (splits.size() != 1) throw new IllegalStateException("Local executor requires exactly one source split");
        SourceReader reader = (SourceReader) source.createReader();
        try {
            reader.open((SourceSplit) splits.get(0));
            while (!reader.isFinished()) {
                RecordBatch<FluxRow> batch = (RecordBatch<FluxRow>) reader.pollBatch();
                if (!batch.isEndOfInput() && !batch.isEmpty()) {
                    channel.put(batch);
                    count.addAndGet(batch.records().size());
                }
            }
        } finally {
            reader.close();
        }
    }

    private static void write(SinkWriter<FluxRow> sink, Channel<RecordBatch<FluxRow>> channel, AtomicLong count) throws Exception {
        try {
            sink.open();
            while (true) {
                RecordBatch<FluxRow> batch = channel.take();
                if (batch.isEndOfInput()) break;
                sink.write(batch);
                count.addAndGet(batch.records().size());
            }
            sink.flush();
            sink.commit();
        } catch (Exception failure) {
            try {
                sink.rollback();
            } catch (Exception rollbackFailure) {
                failure.addSuppressed(rollbackFailure);
            }
            throw failure;
        } finally {
            sink.close();
        }
    }

    public JobResult execute(PhysicalPlan plan) throws Exception {
        long started = System.currentTimeMillis();
        final Channel<RecordBatch<FluxRow>> channel = new MemoryChannel<RecordBatch<FluxRow>>(plan.channelCapacity());
        final AtomicLong read = new AtomicLong(), written = new AtomicLong();
        final AtomicReference<Throwable> failure = new AtomicReference<Throwable>();
        Thread reader = new Thread(new Runnable() {
            public void run() {
                try {
                    read(plan.source(), channel, read);
                } catch (Throwable t) {
                    failure.compareAndSet(null, t);
                } finally {
                    try {
                        channel.put(RecordBatch.<FluxRow>endOfInput());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }, "baize-reader");
        Thread writer = new Thread(new Runnable() {
            public void run() {
                try {
                    write(plan.sink(), channel, written);
                } catch (Throwable t) {
                    failure.compareAndSet(null, t);
                }
            }
        }, "baize-writer");
        reader.start();
        writer.start();
        while (reader.isAlive() || writer.isAlive()) {
            reader.join(25);
            writer.join(25);
            if (failure.get() != null) {
                reader.interrupt();
                writer.interrupt();
            }
        }
        if (failure.get() != null) {
            throw new Exception("Job execution failed", failure.get());
        }
        return new JobResult(read.get(), written.get(), 0, System.currentTimeMillis() - started);
    }

}