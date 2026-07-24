package com.baize.flux.framework.job;

import com.baize.flux.api.sink.CommitScope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Aggregated durability outcome for all SinkTasks in a job. */
public final class CommitSummary {

    private final int committedTaskCount;
    private final int failedOrUncommittedTaskCount;
    private final boolean partialCommit;
    private final CommitScope commitScope;
    private final List<String> retryAdvice;

    public CommitSummary(int committedTaskCount, int failedOrUncommittedTaskCount,
                         CommitScope commitScope, List<String> retryAdvice) {
        if (committedTaskCount < 0 || failedOrUncommittedTaskCount < 0) {
            throw new IllegalArgumentException("commit task counts must not be negative");
        }
        this.committedTaskCount = committedTaskCount;
        this.failedOrUncommittedTaskCount = failedOrUncommittedTaskCount;
        this.partialCommit = committedTaskCount > 0 && failedOrUncommittedTaskCount > 0;
        this.commitScope = Objects.requireNonNull(commitScope, "commitScope must not be null");
        this.retryAdvice = Collections.unmodifiableList(new ArrayList<String>(
                Objects.requireNonNull(retryAdvice, "retryAdvice must not be null")));
    }

    public static CommitSummary empty() {
        return new CommitSummary(0, 0, CommitScope.TASK_LOCAL, Collections.<String>emptyList());
    }
    public int getCommittedTaskCount() { return committedTaskCount; }
    public int getFailedOrUncommittedTaskCount() { return failedOrUncommittedTaskCount; }
    public boolean isPartialCommit() { return partialCommit; }
    public CommitScope getCommitScope() { return commitScope; }
    public List<String> getRetryAdvice() { return retryAdvice; }

    /** Explicitly states why a partial result must not be treated as a global rollback. */
    public String getWarning() {
        return partialCommit
                ? "Some SinkTasks committed while others failed or remained uncommitted. "
                + "The Job is FAILED; TASK_LOCAL commits do not provide Job-level atomicity or a global rollback."
                : null;
    }
}
