package com.baize.flux.framework.metrics;

import com.baize.flux.framework.execution.TaskId;
import com.baize.flux.framework.execution.TaskState;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 单个 Task 的运行指标。
 */
public final class TaskMetrics {

    private final TaskId taskId;

    private final AtomicReference<TaskState> state =
            new AtomicReference<TaskState>(
                    TaskState.CREATED);

    private final AtomicLong batchCount =
            new AtomicLong();

    private final AtomicLong recordCount =
            new AtomicLong();

    private volatile long startTimeMillis;

    private volatile long endTimeMillis;

    public TaskMetrics(TaskId taskId) {
        this.taskId =
                Objects.requireNonNull(
                        taskId,
                        "taskId must not be null");
    }

    public void markStarted() {
        state.set(TaskState.RUNNING);
        startTimeMillis = System.currentTimeMillis();
    }

    public void markFinished(TaskState finalState) {
        state.set(finalState);
        endTimeMillis = System.currentTimeMillis();
    }

    public void incrementBatchCount() {
        batchCount.incrementAndGet();
    }

    public void addRecordCount(long count) {
        if (count > 0) {
            recordCount.addAndGet(count);
        }
    }

    public TaskId getTaskId() {
        return taskId;
    }

    public TaskState getState() {
        return state.get();
    }

    public long getBatchCount() {
        return batchCount.get();
    }

    public long getRecordCount() {
        return recordCount.get();
    }

    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public long getEndTimeMillis() {
        return endTimeMillis;
    }

    public long getDurationMillis() {
        if (startTimeMillis <= 0) {
            return 0L;
        }

        long end =
                endTimeMillis > 0
                        ? endTimeMillis
                        : System.currentTimeMillis();

        return Math.max(0L, end - startTimeMillis);
    }
}