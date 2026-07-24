package com.baize.flux.framework.execution;

import java.util.Objects;

/**
 * Creates filesystem-safe, per-task log file names.
 */
public final class TaskLogFileName {

    private TaskLogFileName() {
    }

    public static String create(String jobName, TaskId taskId, long runId) {
        Objects.requireNonNull(jobName, "jobName must not be null");
        Objects.requireNonNull(taskId, "taskId must not be null");
        if (runId < 0) {
            throw new IllegalArgumentException("runId must not be negative");
        }
        return "job-" + sanitize(jobName)
                + "-" + sanitize(taskId.getPipelineId())
                + "-" + taskId.getTaskType().name().toLowerCase()
                + "-" + taskId.getSubtaskIndex()
                + "-" + runId + ".log";
    }

    private static String sanitize(String value) {
        String sanitized = value.trim().replaceAll("[^A-Za-z0-9._-]+", "_");
        return sanitized.isEmpty() ? "unnamed" : sanitized;
    }
}
