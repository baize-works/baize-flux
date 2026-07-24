package com.baize.flux.framework.execution;

import com.baize.flux.api.source.SourceSplit;
import com.baize.flux.api.table.type.FluxRow;
import com.baize.flux.framework.channel.*;
import com.baize.flux.framework.execution.task.*;
import com.baize.flux.framework.execution.split.SplitProvider;
import com.baize.flux.framework.metrics.JobMetrics;
import com.baize.flux.framework.planner.*;
import com.baize.flux.framework.routing.Partitioner;
import com.baize.flux.framework.job.CommitSummary;
import com.baize.flux.framework.job.PipelineResult;
import com.baize.flux.framework.job.PipelineStatus;
import java.util.*;

/** Executes one Pipeline with channels and writers owned exclusively by that Pipeline. */
final class PipelineExecution {
    private final PipelinePlan plan; private final com.baize.flux.framework.job.ExecutionConfig config; private final CancellationToken token; private final JobMetrics metrics; private final ClassLoader loader;
    PipelineExecution(PipelinePlan plan, com.baize.flux.framework.job.ExecutionConfig config, CancellationToken token, JobMetrics metrics, ClassLoader loader) { this.plan=plan; this.config=config; this.token=token; this.metrics=metrics; this.loader=loader; }
    PipelineResult execute() {
        List<DataChannel<RecordEnvelope<FluxRow>>> channels = new ArrayList<DataChannel<RecordEnvelope<FluxRow>>>();
        try {
            for (int i=0;i<plan.getSinkTaskPlans().size();i++) { LocalDataChannel<RecordEnvelope<FluxRow>> c = new LocalDataChannel<RecordEnvelope<FluxRow>>(plan.getPipelineId()+"-source-to-sink-"+i,config.getMaxBufferedBatches(),config.getMaxBufferedRecords(),config.getMaxBufferedBytes(),config.getMaxRecordsPerSecond(),config.getMaxBytesPerSecond(),plan.getSourceTaskPlans().size()); channels.add(c); metrics.registerChannel(c.getMetrics()); }
            List<ExecutionTask> sinks = new ArrayList<ExecutionTask>(); for(int i=0;i<channels.size();i++) sinks.add(new SinkTask(plan.getSinkTaskPlans().get(i),new InputGate<RecordEnvelope<FluxRow>>(channels.get(i).openReader())));
            List<ExecutionTask> sources = new ArrayList<ExecutionTask>(); for(SourceTaskPlan<?> source : plan.getSourceTaskPlans()) { metrics.registerSplitProvider(source.getSplitProvider()); sources.add(createSource(source, channels)); }
            try (TaskExecutor executor = new TaskExecutor(sinks.size()+sources.size(), "baize-flux-"+plan.getPipelineId())) {
                ExecutionCoordinator outcomeCoordinator = new ExecutionCoordinator(executor, token, metrics, loader, new Runnable(){ public void run(){ fail(channels, token.getCause()); cancelProviders(); }});
                ExecutionCoordinator.ExecutionOutcome outcome=outcomeCoordinator.execute(sinks,sources);
                if(outcome.getFailure()!=null) return new PipelineResult(plan.getPipelineId(), plan.getDataSetId(), token.isCancelled() ? PipelineStatus.CANCELED : PipelineStatus.FAILED, outcome.getCommitSummary(), outcome.getFailure());
                return new PipelineResult(plan.getPipelineId(), plan.getDataSetId(), PipelineStatus.SUCCEEDED, outcome.getCommitSummary(), null);
            }
        } finally { for(DataChannel<RecordEnvelope<FluxRow>> c:channels) try { c.close(); } catch(Throwable ignored) {} }
    }
    private void cancelProviders(){ Set<SplitProvider<?>> set=new HashSet<SplitProvider<?>>(); for(SourceTaskPlan<?> p:plan.getSourceTaskPlans()) if(p.getSplitProvider()!=null)set.add(p.getSplitProvider()); for(SplitProvider<?> p:set)p.cancel(token.getCause()); }
    private static void fail(List<DataChannel<RecordEnvelope<FluxRow>>> channels, Throwable cause){ for(DataChannel<RecordEnvelope<FluxRow>> c:channels)c.fail(cause==null?new IllegalStateException("Pipeline cancelled"):cause); }
    private ExecutionTask createSource(SourceTaskPlan<?> plan, List<DataChannel<RecordEnvelope<FluxRow>>> channels){ return typed(plan,channels); }
    private <S extends SourceSplit> ExecutionTask typed(SourceTaskPlan<S> source, List<DataChannel<RecordEnvelope<FluxRow>>> channels) { List<ChannelWriter<RecordEnvelope<FluxRow>>> writers=new ArrayList<ChannelWriter<RecordEnvelope<FluxRow>>>(); for(DataChannel<RecordEnvelope<FluxRow>> c:channels)writers.add(c.openWriter()); Partitioner<RecordEnvelope<FluxRow>> partitioner = new Partitioner<RecordEnvelope<FluxRow>>(){ public int selectChannel(RecordEnvelope<FluxRow> v,int count){ return count == 1 ? 0 : Math.floorMod(v.getBatch().getSplitId().hashCode(),count); }}; return new SourceTask<S>(source,new OutputGate<RecordEnvelope<FluxRow>>(writers,partitioner)); }
    static final class PipelineFailure extends RuntimeException { final CommitSummary summary; PipelineFailure(Throwable cause, CommitSummary summary){super(cause);this.summary=summary;} }
}
