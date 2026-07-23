package com.baize.flux.framework.metrics;

import com.baize.flux.framework.execution.TaskId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Job 级运行指标。
 */
public final class JobMetrics {

    private final Map<TaskId, TaskMetrics> taskMetrics =
            new ConcurrentHashMap<TaskId, TaskMetrics>();

    private final List<ChannelMetrics> channelMetrics =
            new CopyOnWriteArrayList<ChannelMetrics>();

    public TaskMetrics registerTask(TaskId taskId) {
        TaskMetrics created =
                new TaskMetrics(taskId);

        TaskMetrics previous =
                taskMetrics.putIfAbsent(
                        taskId,
                        created);

        return previous == null
                ? created
                : previous;
    }

    public void registerChannel(
            ChannelMetrics metrics) {

        channelMetrics.add(metrics);
    }

    public Map<TaskId, TaskMetrics> getTaskMetrics() {
        return Collections.unmodifiableMap(
                new LinkedHashMap<TaskId, TaskMetrics>(
                        taskMetrics));
    }

    public List<ChannelMetrics> getChannelMetrics() {
        return Collections.unmodifiableList(
                new ArrayList<ChannelMetrics>(
                        channelMetrics));
    }

    public long getSourceRecordCount() {
        return sumRecords("source");
    }

    public long getSinkRecordCount() {
        return sumRecords("sink");
    }

    public long getSourceBatchCount() {
        return sumBatches("source");
    }

    public long getSinkBatchCount() {
        return sumBatches("sink");
    }

    private long sumRecords(String stageName) {
        long total = 0L;

        for (TaskMetrics metrics : taskMetrics.values()) {
            if (stageName.equals(
                    metrics.getTaskId().getStageName())) {

                total += metrics.getRecordCount();
            }
        }

        return total;
    }

    private long sumBatches(String stageName) {
        long total = 0L;

        for (TaskMetrics metrics : taskMetrics.values()) {
            if (stageName.equals(
                    metrics.getTaskId().getStageName())) {

                total += metrics.getBatchCount();
            }
        }

        return total;
    }
}