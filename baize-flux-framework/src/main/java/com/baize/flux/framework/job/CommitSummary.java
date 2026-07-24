package com.baize.flux.framework.job;

import com.baize.flux.api.sink.CommitScope;

import java.util.Objects;

/**
 * Immutable summary of independently committed sink tasks.
 */
public final class CommitSummary {

    private final int committedTaskCount;
    private final int failedOrUncommittedTaskCount;
    private final boolean partialCommit;
    private final CommitScope commitScope;
    private final String retryAdvice;

    public CommitSummary(int committedTaskCount, int failedOrUncommittedTaskCount,
                         CommitScope commitScope, String retryAdvice) {
        this.committedTaskCount = committedTaskCount;
        this.failedOrUncommittedTaskCount = failedOrUncommittedTaskCount;
        this.partialCommit = committedTaskCount > 0 && failedOrUncommittedTaskCount > 0;
        this.commitScope = Objects.requireNonNull(commitScope, "commitScope must not be null");
        this.retryAdvice = Objects.requireNonNull(retryAdvice, "retryAdvice must not be null");
    }

    public static CommitSummary empty() {
        return new CommitSummary(0, 0, CommitScope.TASK_LOCAL,
                "No sink tasks were executed.");
    }

    public int getCommittedTaskCount() {
        return committedTaskCount;
    }

    public int getFailedOrUncommittedTaskCount() {
        return failedOrUncommittedTaskCount;
    }

    public boolean isPartialCommit() {
        return partialCommit;
    }

    public CommitScope getCommitScope() {
        return commitScope;
    }

    public String getRetryAdvice() {
        return retryAdvice;
    }

    /**
     * Explicitly states why a partial task-local commit cannot be rolled back globally.
     */
    public String getWarning() {
        if (!partialCommit) return "";
        return "Partial task-local commit detected; ordinary JDBC sinks do not provide Job-level atomicity or global rollback.";
    }
}
