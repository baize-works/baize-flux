package com.baize.flux.framework.execution;

import com.baize.flux.api.dirtydata.DirtyDataSummary;
import com.baize.flux.api.sink.CommitScope;
import com.baize.flux.framework.job.CommitSummary;
import com.baize.flux.framework.job.JobResult;
import com.baize.flux.framework.job.JobStatus;
import com.baize.flux.framework.job.PipelineResult;
import com.baize.flux.framework.metrics.JobMetrics;
import com.baize.flux.framework.planner.ExecutionPlan;
import com.baize.flux.framework.planner.PipelinePlan;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * Coordinates pipelines by completion order and retains every submitted outcome.
 */
public final class JobExecution {
    private final ExecutionPlan executionPlan;
    private final ClassLoader classLoader;
    private final CancellationToken cancellationToken = new CancellationToken();
    private final JobMetrics jobMetrics = new JobMetrics();

    public JobExecution(ExecutionPlan p, ClassLoader l) {
        executionPlan = Objects.requireNonNull(p, "executionPlan");
        classLoader = Objects.requireNonNull(l, "classLoader");
    }

    public JobResult execute() {
        long start = System.currentTimeMillis();
        List<PipelineResult> results = new ArrayList<PipelineResult>();
        Throwable first = null;
        if (!executionPlan.isEmpty()) {
            int n = Math.min(executionPlan.getExecutionConfig().getPipelineParallelism(), executionPlan.getPipelinePlans().size());
            ExecutorService pool = Executors.newFixedThreadPool(n);
            CompletionService<PipelineResult> cs = new ExecutorCompletionService<PipelineResult>(pool);
            List<Future<PipelineResult>> submitted = new ArrayList<Future<PipelineResult>>();
            try {
                for (final PipelinePlan p : executionPlan.getPipelinePlans())
                    submitted.add(cs.submit(new Callable<PipelineResult>() {
                        public PipelineResult call() {
                            return new PipelineExecution(p, executionPlan.getExecutionConfig(), cancellationToken, jobMetrics, classLoader, executionPlan.getJobName(), start).execute();
                        }
                    }));
                for (int i = 0; i < submitted.size(); i++) {
                    try {
                        PipelineResult r = cs.take().get();
                        results.add(r);
                        if (r.getFailure() != null && first == null) {
                            first = r.getFailure();
                            cancellationToken.cancel(first);
                        }
                    } catch (Exception e) {
                        Throwable x = e instanceof ExecutionException && e.getCause() != null ? e.getCause() : e;
                        if (first == null) {
                            first = x;
                            cancellationToken.cancel(x);
                        }
                    }
                }
            } finally {
                pool.shutdownNow();
            }
        }
        CommitSummary summary = merge(results);
        JobStatus status = first != null ? JobStatus.FAILED : cancellationToken.isCancelled() ? JobStatus.CANCELED : JobStatus.SUCCEEDED;
        return new JobResult(executionPlan.getJobName(), status, start, System.currentTimeMillis(), jobMetrics, first, summary, DirtyDataSummary.empty(), results);
    }

    private CommitSummary merge(List<PipelineResult> rs) {
        int total = 0, finished = 0, committed = 0, empty = 0, failed = 0;
        long attempted = 0, written = 0, committedRows = 0, failedRows = 0, unknown = 0;
        for (PipelineResult r : rs) {
            CommitSummary s = r.getCommitSummary();
            total += s.getTotalTaskCount();
            finished += s.getFinishedTaskCount();
            committed += s.getCommittedTaskCount();
            empty += s.getEmptyCommittedTaskCount();
            failed += s.getFailedOrUncommittedTaskCount();
            attempted += s.getAttemptedRecordCount();
            written += s.getSuccessfullyWrittenRecordCount();
            committedRows += s.getSuccessfullyCommittedRecordCount();
            failedRows += s.getFailedRecordCount();
            unknown += s.getUnknownStateRecordCount();
        }
        return new CommitSummary(total, finished, committed, empty, failed, attempted, written, committedRows, failedRows, unknown, CommitScope.TASK_LOCAL, "This sink commits per task; inspect unknown batch states before retrying.");
    }

    public void cancel() {
        cancellationToken.cancel(new CancellationException("Job was cancelled by caller"));
    }

    /**
     * 返回当前 Job 的实时指标。
     *
     * <p>JobMetrics 本身是线程安全的，调用方应将其转换为只读快照，
     * 不应直接暴露给 HTTP 客户端。
     */
    public JobMetrics getMetrics() {
        return jobMetrics;
    }

    /**
     * 当前 Job 是否已经收到取消请求。
     */
    public boolean isCancellationRequested() {
        return cancellationToken.isCancelled();
    }
}
