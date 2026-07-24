package com.baize.flux.framework.execution;

import com.baize.flux.api.source.SourceSplit;
import com.baize.flux.api.table.type.FluxRow;
import com.baize.flux.framework.channel.ChannelReader;
import com.baize.flux.framework.channel.ChannelWriter;
import com.baize.flux.framework.channel.DataChannel;
import com.baize.flux.framework.channel.InputGate;
import com.baize.flux.framework.channel.LocalDataChannel;
import com.baize.flux.framework.channel.OutputGate;
import com.baize.flux.framework.channel.RecordEnvelope;
import com.baize.flux.framework.execution.task.ExecutionTask;
import com.baize.flux.framework.execution.task.SinkTask;
import com.baize.flux.framework.execution.task.SourceTask;
import com.baize.flux.framework.job.JobResult;
import com.baize.flux.framework.job.JobStatus;
import com.baize.flux.framework.job.CommitSummary;
import com.baize.flux.framework.metrics.JobMetrics;
import com.baize.flux.framework.planner.ExecutionPlan;
import com.baize.flux.framework.planner.SinkTaskPlan;
import com.baize.flux.framework.planner.SourceTaskPlan;
import com.baize.flux.framework.execution.split.SplitProvider;
import com.baize.flux.framework.routing.Partitioner;
import com.baize.flux.framework.routing.SinkPartitioner;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 一次 Job 的本地执行实例。
 */
public final class JobExecution {

    private final ExecutionPlan executionPlan;

    private final ClassLoader classLoader;

    private final CancellationToken cancellationToken =
            new CancellationToken();

    private final JobMetrics jobMetrics =
            new JobMetrics();

    public JobExecution(
            ExecutionPlan executionPlan,
            ClassLoader classLoader) {

        this.executionPlan =
                Objects.requireNonNull(
                        executionPlan,
                        "executionPlan must not be null");

        this.classLoader =
                Objects.requireNonNull(
                        classLoader,
                        "classLoader must not be null");
    }

    public JobResult execute() {
        long startTime =
                System.currentTimeMillis();

        if (executionPlan.isEmpty()) {
            return new JobResult(
                    executionPlan.getJobName(),
                    JobStatus.SUCCEEDED,
                    startTime,
                    System.currentTimeMillis(),
                    jobMetrics,
                    null);
        }

        List<DataChannel<RecordEnvelope<FluxRow>>>
                channels =
                new ArrayList<
                        DataChannel<RecordEnvelope<FluxRow>>>();

        Throwable failure = null;
        CommitSummary commitSummary = CommitSummary.empty();

        try {
            int sourceTaskCount =
                    executionPlan
                            .getSourceTaskPlans()
                            .size();

            int sinkTaskCount =
                    executionPlan
                            .getSinkTaskPlans()
                            .size();

            createChannels(
                    channels,
                    sourceTaskCount,
                    sinkTaskCount);

            List<ExecutionTask> sinkTasks =
                    createSinkTasks(channels);

            List<ExecutionTask> sourceTasks =
                    createSourceTasks(channels);

            for (SourceTaskPlan<?> plan : executionPlan.getSourceTaskPlans()) {
                jobMetrics.registerSplitProvider(plan.getSplitProvider());
            }

            int totalTaskCount =
                    sinkTasks.size()
                            + sourceTasks.size();

            try (TaskExecutor taskExecutor =
                         new TaskExecutor(
                                 totalTaskCount,
                                 "baize-flux-task")) {

                ExecutionCoordinator coordinator =
                        new ExecutionCoordinator(
                                taskExecutor,
                                cancellationToken,
                                jobMetrics,
                                classLoader,
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        failChannels(channels, cancellationToken.getCause());
                                        cancelSplitProviders(executionPlan.getSourceTaskPlans(), cancellationToken.getCause());
                                    }
                                });

                ExecutionCoordinator.ExecutionOutcome outcome = coordinator.execute(sinkTasks, sourceTasks);
                failure = outcome.getFailure();
                commitSummary = outcome.getCommitSummary();
            }

        } catch (Throwable throwable) {
            failure = throwable;

            cancellationToken.cancel(
                    throwable);

            failChannels(
                    channels,
                    throwable);

        } finally {
            closeChannels(channels);
        }

        JobStatus status;

        if (commitSummary.isPartialCommit()) {
            /* A partial task-local commit must never be presented as a successful or canceled Job. */
            status = JobStatus.FAILED;
            if (failure == null) {
                failure = new IllegalStateException(commitSummary.getWarning());
            }
        } else if (failure != null) {
            status = JobStatus.FAILED;
        } else if (cancellationToken.isCancelled()) {
            status = JobStatus.CANCELED;
        } else {
            status = JobStatus.SUCCEEDED;
        }

        return new JobResult(
                executionPlan.getJobName(),
                status,
                startTime,
                System.currentTimeMillis(),
                jobMetrics,
                failure,
                commitSummary);
    }

    public void cancel() {
        cancellationToken.cancel(
                new java.util.concurrent.CancellationException(
                        "Job was cancelled by caller"));
    }

    private static void cancelSplitProviders(List<SourceTaskPlan<?>> plans, Throwable cause) {
        java.util.HashSet<SplitProvider<?>> providers = new java.util.HashSet<SplitProvider<?>>();
        for (SourceTaskPlan<?> plan : plans) if (plan.getSplitProvider() != null) providers.add(plan.getSplitProvider());
        for (SplitProvider<?> provider : providers) provider.cancel(cause);
    }

    private void createChannels(
            List<DataChannel<RecordEnvelope<FluxRow>>>
                    channels,
            int sourceTaskCount,
            int sinkTaskCount) {

        for (int i = 0; i < sinkTaskCount; i++) {
            LocalDataChannel<RecordEnvelope<FluxRow>>
                    channel =
                    new LocalDataChannel<
                            RecordEnvelope<FluxRow>>(
                            "source-to-sink-" + i,
                            executionPlan
                                    .getExecutionConfig()
                                    .getMaxBufferedBatches(),
                            executionPlan.getExecutionConfig().getMaxBufferedRecords(),
                            executionPlan.getExecutionConfig().getMaxBufferedBytes(),
                            executionPlan.getExecutionConfig().getMaxRecordsPerSecond(),
                            executionPlan.getExecutionConfig().getMaxBytesPerSecond(),
                            sourceTaskCount);

            channels.add(channel);

            jobMetrics.registerChannel(
                    channel.getMetrics());
        }
    }

    private List<ExecutionTask> createSinkTasks(
            List<DataChannel<RecordEnvelope<FluxRow>>>
                    channels) {

        List<SinkTaskPlan> plans =
                executionPlan.getSinkTaskPlans();

        if (plans.size() != channels.size()) {
            throw new IllegalStateException(
                    "Sink task count does not match channel count");
        }

        List<ExecutionTask> tasks =
                new ArrayList<ExecutionTask>(
                        plans.size());

        for (int i = 0; i < plans.size(); i++) {
            ChannelReader<RecordEnvelope<FluxRow>>
                    reader =
                    channels.get(i)
                            .openReader();

            InputGate<RecordEnvelope<FluxRow>>
                    inputGate =
                    new InputGate<RecordEnvelope<FluxRow>>(
                            reader);

            tasks.add(
                    new SinkTask(
                            plans.get(i),
                            inputGate));
        }

        return tasks;
    }

    private List<ExecutionTask> createSourceTasks(
            List<DataChannel<RecordEnvelope<FluxRow>>>
                    channels) {

        List<ExecutionTask> tasks =
                new ArrayList<ExecutionTask>();

        for (SourceTaskPlan<?> plan
                : executionPlan.getSourceTaskPlans()) {

            tasks.add(
                    createSourceTask(
                            plan,
                            channels));
        }

        return tasks;
    }

    private ExecutionTask createSourceTask(
            SourceTaskPlan<?> plan,
            List<DataChannel<RecordEnvelope<FluxRow>>>
                    channels) {

        return createTypedSourceTask(
                plan,
                channels);
    }

    private <SplitT extends SourceSplit>
    ExecutionTask createTypedSourceTask(
            SourceTaskPlan<SplitT> plan,
            List<DataChannel<RecordEnvelope<FluxRow>>>
                    channels) {

        List<ChannelWriter<RecordEnvelope<FluxRow>>>
                writers =
                new ArrayList<
                        ChannelWriter<RecordEnvelope<FluxRow>>>(
                        channels.size());

        for (DataChannel<RecordEnvelope<FluxRow>>
                channel : channels) {

            writers.add(
                    channel.openWriter());
        }

        /*
         * 当前默认按表 Hash 路由。
         * 同一个表始终进入相同 SinkTask。
         */
        Partitioner<RecordEnvelope<FluxRow>>
                partitioner =
                new SinkPartitioner<FluxRow>(executionPlan.getExecutionConfig().getSinkPartitionStrategy());

        OutputGate<RecordEnvelope<FluxRow>>
                outputGate =
                new OutputGate<RecordEnvelope<FluxRow>>(
                        writers,
                        partitioner);

        return new SourceTask<SplitT>(
                plan,
                outputGate);
    }

    private void failChannels(
            List<DataChannel<RecordEnvelope<FluxRow>>>
                    channels,
            Throwable cause) {

        Throwable effectiveCause =
                cause == null
                        ? new IllegalStateException(
                        "Job execution cancelled")
                        : cause;

        for (DataChannel<RecordEnvelope<FluxRow>>
                channel : channels) {

            channel.fail(
                    effectiveCause);
        }
    }

    private void closeChannels(
            List<DataChannel<RecordEnvelope<FluxRow>>>
                    channels) {

        for (DataChannel<RecordEnvelope<FluxRow>>
                channel : channels) {

            try {
                channel.close();
            } catch (Throwable ignored) {
                // Channel close should not hide primary failure.
            }
        }
    }
}
