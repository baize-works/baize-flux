package com.baize.flux.framework.connector;

import com.baize.flux.framework.job.ExecutionConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 已完成 Connector 准备的 Job。
 */
public final class PreparedJob {

    private final String jobName;

    private final PreparedSource<?> source;

    private final List<PreparedSink> sinks;

    private final ExecutionConfig executionConfig;

    public PreparedJob(
            String jobName,
            PreparedSource<?> source,
            List<PreparedSink> sinks,
            ExecutionConfig executionConfig) {

        this.jobName =
                Objects.requireNonNull(
                        jobName,
                        "jobName must not be null");

        this.source =
                Objects.requireNonNull(
                        source,
                        "source must not be null");

        this.sinks =
                Collections.unmodifiableList(
                        new ArrayList<PreparedSink>(
                                Objects.requireNonNull(
                                        sinks,
                                        "sinks must not be null")));

        if (this.sinks.isEmpty()) {
            throw new IllegalArgumentException(
                    "sinks must not be empty");
        }

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

    public List<PreparedSink> getSinks() {
        return sinks;
    }

    public ExecutionConfig getExecutionConfig() {
        return executionConfig;
    }
}
