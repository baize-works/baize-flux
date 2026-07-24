package com.baize.flux.framework.execution.split;

import com.baize.flux.api.source.SourceSplit;
import com.baize.flux.framework.execution.CancellationToken;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CancellationException;

/** Thread-safe, in-process provider for dynamically assigned source splits. */
public final class LocalSplitQueue<SplitT extends SourceSplit> {

    private enum State { PENDING, RUNNING, COMPLETED, FAILED }

    private final ArrayDeque<SplitT> pending = new ArrayDeque<SplitT>();
    private final Map<String, State> states = new HashMap<String, State>();
    private boolean cancelled;
    private Throwable cancellationCause;

    public LocalSplitQueue(Collection<SplitT> splits) {
        Objects.requireNonNull(splits, "splits must not be null");
        for (SplitT split : splits) {
            Objects.requireNonNull(split, "split must not be null");
            String id = Objects.requireNonNull(split.splitId(), "splitId must not be null");
            if (states.put(id, State.PENDING) != null) {
                throw new IllegalArgumentException("Duplicate split id: " + id);
            }
            pending.addLast(split);
        }
    }

    /** Waits for a split, returning null only after all splits have completed. */
    public synchronized SplitT acquire(CancellationToken token) throws InterruptedException {
        Objects.requireNonNull(token, "token must not be null");
        token.onCancel(new Runnable() {
            @Override public void run() { wakeWaiters(); }
        });
        while (pending.isEmpty() && hasRunningSplits() && !isStopped(token)) {
            wait();
        }
        throwIfStopped(token);
        if (pending.isEmpty()) return null;
        SplitT split = pending.removeFirst();
        states.put(split.splitId(), State.RUNNING);
        return split;
    }

    private synchronized void wakeWaiters() { notifyAll(); }

    public synchronized void complete(SplitT split) {
        transition(split, State.RUNNING, State.COMPLETED, false);
    }

    public synchronized void returnSplit(SplitT split) {
        transition(split, State.RUNNING, State.PENDING, true);
    }

    /** Marks the split failed and wakes every waiting worker. */
    public synchronized void fail(SplitT split, Throwable cause) {
        transition(split, State.RUNNING, State.FAILED, false);
        cancel(cause);
    }

    public synchronized void cancel(Throwable cause) {
        if (!cancelled) {
            cancelled = true;
            cancellationCause = cause;
        }
        notifyAll();
    }

    public synchronized long getTotalSplitCount() { return states.size(); }
    public synchronized long getPendingSplitCount() { return count(State.PENDING); }
    public synchronized long getRunningSplitCount() { return count(State.RUNNING); }
    public synchronized long getCompletedSplitCount() { return count(State.COMPLETED); }
    public synchronized long getFailedSplitCount() { return count(State.FAILED); }

    private void transition(SplitT split, State from, State to, boolean requeue) {
        Objects.requireNonNull(split, "split must not be null");
        String id = split.splitId();
        if (states.get(id) != from) {
            throw new IllegalStateException("Split " + id + " is not " + from);
        }
        states.put(id, to);
        if (requeue) pending.addLast(split);
        notifyAll();
    }

    private boolean hasRunningSplits() { return count(State.RUNNING) > 0; }
    private long count(State state) {
        long count = 0;
        for (State value : states.values()) if (value == state) count++;
        return count;
    }
    private boolean isStopped(CancellationToken token) {
        return cancelled || token.isCancelled() || Thread.currentThread().isInterrupted();
    }
    private void throwIfStopped(CancellationToken token) {
        if (Thread.currentThread().isInterrupted()) throw new CancellationException("Split acquisition was interrupted");
        if (cancelled || token.isCancelled()) {
            CancellationException exception = new CancellationException("Split acquisition was cancelled");
            Throwable cause = cancellationCause != null ? cancellationCause : token.getCause();
            if (cause != null) exception.initCause(cause);
            throw exception;
        }
    }
}
