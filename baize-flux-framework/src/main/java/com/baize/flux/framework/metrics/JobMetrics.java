package com.baize.flux.framework.metrics;

import com.baize.flux.framework.execution.TaskId;
import com.baize.flux.framework.execution.split.LocalSplitQueue;

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
    private volatile LocalSplitQueue<?> splitQueue;
    private volatile long staticTotalSplitCount;

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
        return sumMetric("source", Metric.SOURCE_READ_RECORDS);
    }

    public long getSinkRecordCount() {
        return sumMetric("sink", Metric.SINK_SUCCESS_RECORDS);
    }

    public long getSourceBatchCount() {
        return sumBatches("source");
    }

    public long getSinkBatchCount() {
        return sumBatches("sink");
    }

    /** Positive means more rows have been read than successfully written. */
    public long getSourceSinkRecordDifference() {
        return getSourceRecordCount() - getSinkRecordCount();
    }

    public long getFailedRecordCount() { return sumMetric(null, Metric.FAILED_RECORDS); }
    public long getSkippedRecordCount() { return sumMetric(null, Metric.SKIPPED_RECORDS); }
    public long getSourceReadBytes() { return sumMetric("source", Metric.SOURCE_BYTES); }
    public long getSinkWrittenBytes() { return sumMetric("sink", Metric.SINK_BYTES); }
    public long getCompletedSplitCount() { return sumMetric("source", Metric.COMPLETED_SPLITS); }
    public void registerSplitQueue(LocalSplitQueue<?> queue) { splitQueue = queue; }
    public void setStaticTotalSplitCount(long count) { staticTotalSplitCount = count; }
    public long getTotalSplitCount() { return splitQueue == null ? staticTotalSplitCount : splitQueue.getTotalSplitCount(); }
    public long getPendingSplitCount() { return splitQueue == null ? Math.max(0L, staticTotalSplitCount - getCompletedSplitCount()) : splitQueue.getPendingSplitCount(); }
    public long getRunningSplitCount() { return splitQueue == null ? 0L : splitQueue.getRunningSplitCount(); }
    public long getFailedSplitCount() { return splitQueue == null ? 0L : splitQueue.getFailedSplitCount(); }
    public long getBatchRetryCount() { return sumMetric(null, Metric.BATCH_RETRIES); }
    public long getDatabaseCommitMillis() { return sumMetric("sink", Metric.COMMIT_MILLIS); }
    public long getSqlExecutionMillis() { return sumMetric(null, Metric.SQL_MILLIS); }

    public double getCurrentQps() { return sumRate(false); }
    public double getAverageQps() { return sumRate(true); }

    /** Average of registered channel blocking ratios; zero when no channel is registered. */
    public double getChannelBlockedRatio() {
        if (channelMetrics.isEmpty()) return 0D;
        double total = 0D;
        for (ChannelMetrics metrics : channelMetrics) total += metrics.getBlockedRatio();
        return total / channelMetrics.size();
    }

    private double sumRate(boolean average) {
        double total = 0D;
        for (TaskMetrics metrics : taskMetrics.values())
            total += average ? metrics.getAverageQps() : metrics.getCurrentQps();
        return total;
    }

    private long sumMetric(String stageName, Metric metric) {
        long total = 0L;

        for (TaskMetrics metrics : taskMetrics.values()) {
            if (stageName == null || stageName.equals(metrics.getTaskId().getStageName())) {
                switch (metric) {
                    case SOURCE_READ_RECORDS: total += metrics.getSourceReadRecordCount(); break;
                    case SINK_SUCCESS_RECORDS: total += metrics.getSinkWriteSuccessRecordCount(); break;
                    case FAILED_RECORDS: total += metrics.getFailedRecordCount(); break;
                    case SKIPPED_RECORDS: total += metrics.getSkippedRecordCount(); break;
                    case SOURCE_BYTES: total += metrics.getSourceReadBytes(); break;
                    case SINK_BYTES: total += metrics.getSinkWrittenBytes(); break;
                    case COMPLETED_SPLITS: total += metrics.getCompletedSplitCount(); break;
                    case BATCH_RETRIES: total += metrics.getBatchRetryCount(); break;
                    case COMMIT_MILLIS: total += metrics.getDatabaseCommitMillis(); break;
                    case SQL_MILLIS: total += metrics.getSqlExecutionMillis(); break;
                    default: break;
                }
            }
        }

        return total;
    }

    private enum Metric {
        SOURCE_READ_RECORDS, SINK_SUCCESS_RECORDS, FAILED_RECORDS, SKIPPED_RECORDS,
        SOURCE_BYTES, SINK_BYTES, COMPLETED_SPLITS, BATCH_RETRIES, COMMIT_MILLIS, SQL_MILLIS
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
