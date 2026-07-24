package com.baize.flux.framework.execution;

import com.baize.flux.api.dirtydata.DirtyDataSummary;
import com.baize.flux.framework.job.*;
import com.baize.flux.framework.metrics.JobMetrics;
import com.baize.flux.framework.planner.*;
import java.util.*;
import java.util.concurrent.*;

/** Job coordinator: schedules bounded Pipeline executions and propagates failure job-wide. */
public final class JobExecution {
    private final ExecutionPlan executionPlan; private final ClassLoader classLoader; private final CancellationToken cancellationToken=new CancellationToken(); private final JobMetrics jobMetrics=new JobMetrics();
    public JobExecution(ExecutionPlan plan, ClassLoader loader){executionPlan=Objects.requireNonNull(plan,"executionPlan must not be null");classLoader=Objects.requireNonNull(loader,"classLoader must not be null");}
    public JobResult execute(){ long start=System.currentTimeMillis(); Throwable failure=null; List<CommitSummary> summaries=new ArrayList<CommitSummary>();
        if(!executionPlan.isEmpty()) { int threads=Math.min(executionPlan.getExecutionConfig().getPipelineParallelism(), executionPlan.getPipelinePlans().size()); ExecutorService pool=Executors.newFixedThreadPool(threads); List<Future<CommitSummary>> futures=new ArrayList<Future<CommitSummary>>(); try { for(final PipelinePlan p:executionPlan.getPipelinePlans()) futures.add(pool.submit(new Callable<CommitSummary>(){public CommitSummary call(){return new PipelineExecution(p,executionPlan.getExecutionConfig(),cancellationToken,jobMetrics,classLoader).execute();}})); for(Future<CommitSummary> f:futures)try { summaries.add(f.get()); } catch(Exception e) { failure=e.getCause()==null?e:e.getCause(); if (failure instanceof PipelineExecution.PipelineFailure) summaries.add(((PipelineExecution.PipelineFailure) failure).summary); cancellationToken.cancel(failure); for(Future<CommitSummary> other:futures)other.cancel(true); break; } } finally { pool.shutdownNow(); } }
        int committed=0, total=0; for(CommitSummary s:summaries){committed+=s.getCommittedTaskCount();total+=s.getCommittedTaskCount()+s.getFailedOrUncommittedTaskCount();} CommitSummary summary=new CommitSummary(committed,Math.max(0,total-committed),com.baize.flux.api.sink.CommitScope.TASK_LOCAL,"This sink commits per task; verify already committed targets before retrying."); JobStatus status=failure!=null||summary.isPartialCommit()?JobStatus.FAILED:cancellationToken.isCancelled()?JobStatus.CANCELED:JobStatus.SUCCEEDED; return new JobResult(executionPlan.getJobName(),status,start,System.currentTimeMillis(),jobMetrics,failure,summary,DirtyDataSummary.empty()); }
    public void cancel(){cancellationToken.cancel(new CancellationException("Job was cancelled by caller"));}
}
