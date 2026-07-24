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

/** 以中文分类展示已完成作业的汇总、任务和通道指标。 */
public final class JobResultPrinter {

    private JobResultPrinter() {
    }

    public static void print(JobResult result) {
        print(result, System.out);
    }

    static void print(JobResult result, PrintStream output) {
        JobMetrics metrics = result.getMetrics();
        output.println("Flux 作业执行完成：");
        line(output, "作业名称", result.getJobName());
        line(output, "执行状态", result.getStatus());
        line(output, "执行耗时（毫秒）", result.getDurationMillis());

        output.println("  汇总指标：");
        output.println("    数据源：");
        line(output, "批次数", metrics.getSourceBatchCount());
        line(output, "读取记录数", metrics.getSourceRecordCount());
        line(output, "读取字节数", metrics.getSourceReadBytes());
        line(output, "已完成分片数", metrics.getCompletedSplitCount());
        output.println("    通道：");
        line(output, "源端与目标端记录数差", metrics.getSourceSinkRecordDifference());
        line(output, "通道阻塞占比", formatRatio(metrics.getChannelBlockedRatio()));
        output.println("    数据目标端：");
        line(output, "批次数", metrics.getSinkBatchCount());
        line(output, "写入记录数", metrics.getSinkRecordCount());
        line(output, "写入字节数", metrics.getSinkWrittenBytes());
        line(output, "数据库提交耗时（毫秒）", metrics.getDatabaseCommitMillis());
        output.println("    执行与异常：");
        line(output, "失败记录数", metrics.getFailedRecordCount());
        line(output, "跳过记录数", metrics.getSkippedRecordCount());
        line(output, "批次重试次数", metrics.getBatchRetryCount());
        line(output, "SQL 执行耗时（毫秒）", metrics.getSqlExecutionMillis());
        line(output, "平均处理速率（条/秒）", formatRate(metrics.getAverageQps()));

        printTaskMetrics(metrics, output);
        printChannelMetrics(metrics, output);
        if (result.getFailure() != null) line(output, "失败原因", result.getFailure());
    }

    private static void printTaskMetrics(JobMetrics metrics, PrintStream output) {
        List<TaskMetrics> tasks = new ArrayList<TaskMetrics>(metrics.getTaskMetrics().values());
        tasks.sort(Comparator.comparing(task -> task.getTaskId().toString()));
        output.println("  任务指标（" + tasks.size() + "）：");
        for (TaskMetrics task : tasks) {
            output.println("    " + task.getTaskId() + "：状态=" + task.getState()
                    + "，批次数=" + task.getBatchCount()
                    + "，记录数=" + task.getRecordCount()
                    + "，源端读取记录数=" + task.getSourceReadRecordCount()
                    + "，目标端写入记录数=" + task.getSinkWriteSuccessRecordCount()
                    + "，失败记录数=" + task.getFailedRecordCount()
                    + "，跳过记录数=" + task.getSkippedRecordCount()
                    + "，已完成分片数=" + task.getCompletedSplitCount()
                    + "，处理速率（条/秒）=" + formatRate(task.getAverageQps())
                    + "，执行耗时（毫秒）=" + task.getDurationMillis()
                    + position(task));
        }
    }

    private static void printChannelMetrics(JobMetrics metrics, PrintStream output) {
        List<ChannelMetrics> channels = new ArrayList<ChannelMetrics>(metrics.getChannelMetrics());
        channels.sort(Comparator.comparing(ChannelMetrics::getChannelId));
        output.println("  通道指标（" + channels.size() + "）：");
        for (ChannelMetrics channel : channels) {
            output.println("    " + channel.getChannelId()
                    + "：入队数=" + channel.getEnqueuedCount()
                    + "，出队数=" + channel.getDequeuedCount()
                    + "，当前队列长度=" + channel.getCurrentSize()
                    + "，最大队列长度=" + channel.getMaximumSize()
                    + "，写入阻塞耗时（毫秒）=" + channel.getWriteBlockedMillis()
                    + "，读取阻塞耗时（毫秒）=" + channel.getReadBlockedMillis()
                    + "，阻塞占比=" + formatRatio(channel.getBlockedRatio()));
        }
    }

    private static String position(TaskMetrics task) {
        if (task.getCurrentTable() == null && task.getCurrentSplit() == null) return "";
        return "，表=" + valueOrDash(task.getCurrentTable())
                + "，分片=" + valueOrDash(task.getCurrentSplit());
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
        output.println("      " + key + "：" + value);
    }
}
