package com.baize.flux.framework.planner;

import com.baize.flux.api.configuration.ConfigValidator;
import com.baize.flux.api.factory.*;
import com.baize.flux.api.job.JobDefinition;
import com.baize.flux.api.source.BoundedSource;
import com.baize.flux.api.table.FluxRow;
import com.baize.flux.framework.plugin.FactoryRegistry;

/**
 * Validates connector options and transforms a bounded job definition into a local plan.
 */
public final class JobPlanner {
    private final FactoryRegistry registry;

    public JobPlanner(FactoryRegistry registry) {
        this.registry = registry;
    }

    public PhysicalPlan plan(JobDefinition job) {
        SourceFactory sf = registry.getSourceFactory(job.sourceType());
        SinkFactory kf = registry.getSinkFactory(job.sinkType());
        ConfigValidator.strict().validate(job.sourceOptions(), sf.optionRule()).throwIfInvalid();
        ConfigValidator.strict().validate(job.sinkOptions(), kf.optionRule()).throwIfInvalid();
        BoundedSource<FluxRow, ?> source = sf.createSource(job.sourceOptions());
        return new PhysicalPlan(source, kf.createSink(job.sinkOptions()), 10);
    }
}
