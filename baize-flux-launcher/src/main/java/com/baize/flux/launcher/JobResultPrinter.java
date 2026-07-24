package com.baize.flux.launcher;

import com.baize.flux.framework.metrics.ChannelMetrics;
import com.baize.flux.framework.metrics.JobMetrics;
import com.baize.flux.framework.metrics.TaskMetrics;
import com.baize.flux.framework.job.JobResult;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Formats a completed job result, including aggregate, task, and channel metrics. */
public final class JobResultPrinter {

    private JobResultPrinter() {
    }

    public static void print(JobResult result) {
        print(result, System.out);
    }

    static void print(JobResult result, PrintStream output) {
        JobMetrics metrics = result.getMetrics();
        output.println("Flux job finished:");
        line(output, "job-name", result.getJobName());
        line(output, "status", result.getStatus());
        line(output, "duration-ms", result.getDurationMillis());

        output.println("  aggregate-metrics:");
        line(output, "source-batches", metrics.getSourceBatchCount());
        line(output, "source-records", metrics.getSourceRecordCount());
        line(output, "source-bytes", metrics.getSourceReadBytes());
        line(output, "completed-splits", metrics.getCompletedSplitCount());
        line(output, "sink-batches", metrics.getSinkBatchCount());
        line(output, "sink-records", metrics.getSinkRecordCount());
        line(output, "sink-bytes", metrics.getSinkWrittenBytes());
        line(output, "source-sink-record-difference", metrics.getSourceSinkRecordDifference());
        line(output, "failed-records", metrics.getFailedRecordCount());
        line(output, "skipped-records", metrics.getSkippedRecordCount());
        line(output, "batch-retries", metrics.getBatchRetryCount());
        line(output, "database-commit-ms", metrics.getDatabaseCommitMillis());
        line(output, "sql-execution-ms", metrics.getSqlExecutionMillis());
        line(output, "average-qps", formatRate(metrics.getAverageQps()));
        line(output, "channel-blocked-ratio", formatRatio(metrics.getChannelBlockedRatio()));

        printTaskMetrics(metrics, output);
        printChannelMetrics(metrics, output);
        if (result.getFailure() != null) line(output, "failure", result.getFailure());
    }

    private static void printTaskMetrics(JobMetrics metrics, PrintStream output) {
        List<TaskMetrics> tasks = new ArrayList<TaskMetrics>(metrics.getTaskMetrics().values());
        tasks.sort(Comparator.comparing(task -> task.getTaskId().toString()));
        output.println("  task-metrics (" + tasks.size() + "):");
        for (TaskMetrics task : tasks) {
            output.println("    " + task.getTaskId() + ": state=" + task.getState()
                    + ", batches=" + task.getBatchCount()
                    + ", records=" + task.getRecordCount()
                    + ", source-records=" + task.getSourceReadRecordCount()
                    + ", sink-records=" + task.getSinkWriteSuccessRecordCount()
                    + ", failed=" + task.getFailedRecordCount()
                    + ", skipped=" + task.getSkippedRecordCount()
                    + ", splits=" + task.getCompletedSplitCount()
                    + ", qps=" + formatRate(task.getAverageQps())
                    + ", duration-ms=" + task.getDurationMillis()
                    + position(task));
        }
    }

    private static void printChannelMetrics(JobMetrics metrics, PrintStream output) {
        List<ChannelMetrics> channels = new ArrayList<ChannelMetrics>(metrics.getChannelMetrics());
        channels.sort(Comparator.comparing(ChannelMetrics::getChannelId));
        output.println("  channel-metrics (" + channels.size() + "):");
        for (ChannelMetrics channel : channels) {
            output.println("    " + channel.getChannelId()
                    + ": enqueued=" + channel.getEnqueuedCount()
                    + ", dequeued=" + channel.getDequeuedCount()
                    + ", current-size=" + channel.getCurrentSize()
                    + ", maximum-size=" + channel.getMaximumSize()
                    + ", write-blocked-ms=" + channel.getWriteBlockedMillis()
                    + ", read-blocked-ms=" + channel.getReadBlockedMillis()
                    + ", blocked-ratio=" + formatRatio(channel.getBlockedRatio()));
        }
    }

    private static String position(TaskMetrics task) {
        if (task.getCurrentTable() == null && task.getCurrentSplit() == null) return "";
        return ", table=" + valueOrDash(task.getCurrentTable())
                + ", split=" + valueOrDash(task.getCurrentSplit());
    }

    private static String valueOrDash(String value) {
        return value == null ? "-" : value;
    }

    private static String formatRate(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private static String formatRatio(double value) {
        return String.format(Locale.ROOT, "%.2f%%", value * 100D);
    }

    private static void line(PrintStream output, String key, Object value) {
        output.println("  " + key + "=" + value);
    }
}
