package com.baize.flux.framework.execution;

import java.util.Objects;

/**
 * Task 唯一标识。
 */
public final class TaskId {

    private final String stageName;

    private final int subtaskIndex;

    private final int parallelism;

    public TaskId(
            String stageName,
            int subtaskIndex,
            int parallelism) {

        if (subtaskIndex < 0) {
            throw new IllegalArgumentException(
                    "subtaskIndex must not be negative");
        }

        if (parallelism <= 0) {
            throw new IllegalArgumentException(
                    "parallelism must be greater than 0");
        }

        if (subtaskIndex >= parallelism) {
            throw new IllegalArgumentException(
                    "subtaskIndex must be less than parallelism");
        }

        this.stageName =
                Objects.requireNonNull(
                        stageName,
                        "stageName must not be null");

        this.subtaskIndex = subtaskIndex;
        this.parallelism = parallelism;
    }

    public String getStageName() {
        return stageName;
    }

    public int getSubtaskIndex() {
        return subtaskIndex;
    }

    public int getParallelism() {
        return parallelism;
    }

    @Override
    public String toString() {
        return stageName
                + "-"
                + subtaskIndex
                + "/"
                + parallelism;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof TaskId)) {
            return false;
        }

        TaskId that = (TaskId) object;

        return subtaskIndex == that.subtaskIndex
                && parallelism == that.parallelism
                && stageName.equals(that.stageName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                stageName,
                subtaskIndex,
                parallelism);
    }
}