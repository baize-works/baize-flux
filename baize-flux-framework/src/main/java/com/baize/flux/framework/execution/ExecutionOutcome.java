package com.baize.flux.framework.execution;

import com.baize.flux.framework.job.CommitSummary;

/** Result returned by the coordinator, including the sink durability outcome. */
public final class ExecutionOutcome {
    private final Throwable failure;
    private final CommitSummary commitSummary;
    public ExecutionOutcome(Throwable failure, CommitSummary commitSummary) {
        this.failure = failure;
        this.commitSummary = commitSummary;
    }
    public Throwable getFailure() { return failure; }
    public CommitSummary getCommitSummary() { return commitSummary; }
}
