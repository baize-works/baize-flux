package com.baize.flux.framework.planner;

import com.baize.flux.framework.job.ExecutionConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 本地执行计划。
 */
public final class ExecutionPlan {

    private final String jobName;

    private final ExecutionConfig executionConfig;

    private final List<SourceTaskPlan<?>> sourceTaskPlans;

    private final List<SinkTaskPlan> sinkTaskPlans;

    public ExecutionPlan(
            String jobName,
            ExecutionConfig executionConfig,
            List<SourceTaskPlan<?>> sourceTaskPlans,
            List<SinkTaskPlan> sinkTaskPlans) {

        this.jobName =
                Objects.requireNonNull(
                        jobName,
                        "jobName must not be null");

        this.executionConfig =
                Objects.requireNonNull(
                        executionConfig,
                        "executionConfig must not be null");

        this.sourceTaskPlans =
                Collections.unmodifiableList(
                        new ArrayList<SourceTaskPlan<?>>(
                                Objects.requireNonNull(
                                        sourceTaskPlans,
                                        "sourceTaskPlans must not be null")));

        this.sinkTaskPlans =
                Collections.unmodifiableList(
                        new ArrayList<SinkTaskPlan>(
                                Objects.requireNonNull(
                                        sinkTaskPlans,
                                        "sinkTaskPlans must not be null")));
    }

    public String getJobName() {
        return jobName;
    }

    public ExecutionConfig getExecutionConfig() {
        return executionConfig;
    }

    public List<SourceTaskPlan<?>> getSourceTaskPlans() {
        return sourceTaskPlans;
    }

    public List<SinkTaskPlan> getSinkTaskPlans() {
        return sinkTaskPlans;
    }

    public boolean isEmpty() {
        return sourceTaskPlans.isEmpty();
    }
}