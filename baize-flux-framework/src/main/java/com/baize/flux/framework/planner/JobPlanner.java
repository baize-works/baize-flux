package com.baize.flux.framework.planner;

import com.baize.flux.api.source.Source;
import com.baize.flux.api.source.SourceSplit;
import com.baize.flux.framework.connector.PreparedJob;
import com.baize.flux.framework.connector.PreparedSink;
import com.baize.flux.framework.connector.PreparedSource;
import com.baize.flux.framework.execution.TaskId;
import com.baize.flux.framework.job.ExecutionConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Job 执行计划生成器。
 */
public final class JobPlanner {

    private final SplitAssigner splitAssigner;

    public JobPlanner() {
        this(new SplitAssigner());
    }

    public JobPlanner(
            SplitAssigner splitAssigner) {

        this.splitAssigner =
                Objects.requireNonNull(
                        splitAssigner,
                        "splitAssigner must not be null");
    }

    public ExecutionPlan plan(
            PreparedJob preparedJob) throws Exception {

        Objects.requireNonNull(
                preparedJob,
                "preparedJob must not be null");

        return createPlan(
                preparedJob,
                preparedJob.getSource());
    }

    private <SplitT extends SourceSplit>
    ExecutionPlan createPlan(
            PreparedJob preparedJob,
            PreparedSource<SplitT> preparedSource) throws Exception {

        ExecutionConfig config =
                preparedJob.getExecutionConfig();

        Source<SplitT> source =
                preparedSource.getSource();

        List<SplitT> splits =
                source.createSplits(
                        preparedSource.getTables(),
                        config.getSourceParallelism());

        if (splits == null) {
            throw new IllegalStateException(
                    "Source returned null splits");
        }

        List<List<SplitT>> assignments =
                splitAssigner.assign(
                        splits,
                        config.getSourceParallelism());

        List<SourceTaskPlan<?>> sourcePlans =
                new ArrayList<SourceTaskPlan<?>>();

        int sourceTaskCount =
                assignments.size();

        for (int i = 0; i < sourceTaskCount; i++) {
            TaskId taskId =
                    new TaskId(
                            "source",
                            i,
                            sourceTaskCount);

            sourcePlans.add(
                    new SourceTaskPlan<SplitT>(
                            taskId,
                            preparedSource,
                            assignments.get(i),
                            config.getBatchSize()));
        }

        List<SinkTaskPlan> sinkPlans =
                new ArrayList<SinkTaskPlan>();

        if (!sourcePlans.isEmpty()) {
            int sinkParallelism =
                    config.getSinkParallelism();

            List<PreparedSink> preparedSinks =
                    preparedJob.getSinks();

            if (preparedSinks.size() != sinkParallelism) {
                throw new IllegalStateException(
                        "Prepared sink count does not match sink parallelism");
            }

            for (int i = 0; i < sinkParallelism; i++) {
                sinkPlans.add(
                        new SinkTaskPlan(
                                new TaskId(
                                        "sink",
                                        i,
                                        sinkParallelism),
                                preparedSinks.get(i)));
            }
        }

        return new ExecutionPlan(
                preparedJob.getJobName(),
                config,
                sourcePlans,
                sinkPlans);
    }
}
