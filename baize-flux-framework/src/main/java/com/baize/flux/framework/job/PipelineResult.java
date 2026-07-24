package com.baize.flux.framework.job;

import java.util.Objects;

/**
 * Final pipeline outcome, returned even for failures so job aggregation is lossless.
 */
public final class PipelineResult {
    private final String pipelineId, dataSetId;
    private final PipelineStatus status;
    private final CommitSummary commitSummary;
    private final Throwable failure;

    public PipelineResult(String pipelineId, String dataSetId, PipelineStatus status, CommitSummary summary, Throwable failure) {
        this.pipelineId = Objects.requireNonNull(pipelineId, "pipelineId");
        this.dataSetId = Objects.requireNonNull(dataSetId, "dataSetId");
        this.status = Objects.requireNonNull(status, "status");
        this.commitSummary = Objects.requireNonNull(summary, "summary");
        this.failure = failure;
    }

    public String getPipelineId() {
        return pipelineId;
    }

    public String getDataSetId() {
        return dataSetId;
    }

    public PipelineStatus getStatus() {
        return status;
    }

    public CommitSummary getCommitSummary() {
        return commitSummary;
    }

    public Throwable getFailure() {
        return failure;
    }
}
