package com.baize.flux.framework.connector;

import com.baize.flux.framework.job.ExecutionConfig;

import java.util.Objects;

/**
 * 已完成 Connector 准备的 Job。
 */
public final class PreparedJob {

    private final String jobName;

    private final PreparedSource<?> source;

    private final PreparedSink sink;

    private final ExecutionConfig executionConfig;

    public PreparedJob(
            String jobName,
            PreparedSource<?> source,
            PreparedSink sink,
            ExecutionConfig executionConfig) {

        this.jobName =
                Objects.requireNonNull(
                        jobName,
                        "jobName must not be null");

        this.source =
                Objects.requireNonNull(
                        source,
                        "source must not be null");

        this.sink =
                Objects.requireNonNull(
                        sink,
                        "sink must not be null");

        this.executionConfig =
                Objects.requireNonNull(
                        executionConfig,
                        "executionConfig must not be null");
    }

    public String getJobName() {
        return jobName;
    }

    public PreparedSource<?> getSource() {
        return source;
    }

    public PreparedSink getSink() {
        return sink;
    }

    public ExecutionConfig getExecutionConfig() {
        return executionConfig;
    }
}