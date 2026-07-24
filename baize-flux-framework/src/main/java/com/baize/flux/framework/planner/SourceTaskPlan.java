package com.baize.flux.framework.planner;

import com.baize.flux.api.source.SourceSplit;
import com.baize.flux.framework.connector.PreparedSource;
import com.baize.flux.framework.execution.TaskId;
import com.baize.flux.framework.execution.split.LocalSplitQueue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * SourceTask 执行计划。
 */
public final class SourceTaskPlan<
        SplitT extends SourceSplit> {

    private final TaskId taskId;

    private final PreparedSource<SplitT> preparedSource;

    private final List<SplitT> splits;

    private final int batchSize;
    private final LocalSplitQueue<SplitT> splitQueue;

    public SourceTaskPlan(
            TaskId taskId,
            PreparedSource<SplitT> preparedSource,
            List<SplitT> splits,
            int batchSize) {
        this(taskId, preparedSource, splits, batchSize, null);
    }

    public SourceTaskPlan(TaskId taskId, PreparedSource<SplitT> preparedSource,
            List<SplitT> splits, int batchSize, LocalSplitQueue<SplitT> splitQueue) {

        if (batchSize <= 0) {
            throw new IllegalArgumentException(
                    "batchSize must be greater than 0");
        }

        this.taskId =
                Objects.requireNonNull(
                        taskId,
                        "taskId must not be null");

        this.preparedSource =
                Objects.requireNonNull(
                        preparedSource,
                        "preparedSource must not be null");

        this.splits =
                Collections.unmodifiableList(
                        new ArrayList<SplitT>(
                                Objects.requireNonNull(
                                        splits,
                                        "splits must not be null")));

        this.batchSize = batchSize;
        this.splitQueue = splitQueue;
    }

    public TaskId getTaskId() {
        return taskId;
    }

    public PreparedSource<SplitT> getPreparedSource() {
        return preparedSource;
    }

    public List<SplitT> getSplits() {
        return splits;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public LocalSplitQueue<SplitT> getSplitQueue() { return splitQueue; }

    public boolean isDynamicSplitAssignment() { return splitQueue != null; }
}
