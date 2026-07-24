package com.baize.flux.framework.execution;

import com.baize.flux.framework.connector.ConnectorPreparer;
import com.baize.flux.framework.connector.FactoryRegistry;
import com.baize.flux.framework.connector.PreparedJob;
import com.baize.flux.framework.job.JobDefinition;
import com.baize.flux.framework.job.JobResult;
import com.baize.flux.framework.planner.ExecutionPlan;
import com.baize.flux.framework.planner.JobPlanner;

import java.util.Objects;
import java.nio.file.Path;

/**
 * 本地离线 Flux 执行引擎。
 */
public final class LocalFluxEngine
        implements FluxEngine {

    private final ClassLoader classLoader;

    private final ConnectorPreparer connectorPreparer;

    private final FactoryRegistry registry;

    private final JobPlanner jobPlanner;

    public LocalFluxEngine(
            ClassLoader classLoader,
            ConnectorPreparer connectorPreparer,
            JobPlanner jobPlanner,
            FactoryRegistry registry) {

        this.classLoader =
                Objects.requireNonNull(
                        classLoader,
                        "classLoader must not be null");

        this.connectorPreparer =
                Objects.requireNonNull(
                        connectorPreparer,
                        "connectorPreparer must not be null");

        this.registry = Objects.requireNonNull(registry, "registry must not be null");

        this.jobPlanner =
                Objects.requireNonNull(
                        jobPlanner,
                        "jobPlanner must not be null");
    }

    public static LocalFluxEngine create(
            ClassLoader classLoader) {
        return create(classLoader, new Path[0]);
    }

    public static LocalFluxEngine create(ClassLoader classLoader, Path... pluginDirectories) {

        ClassLoader effectiveClassLoader =
                classLoader == null
                        ? Thread.currentThread()
                        .getContextClassLoader()
                        : classLoader;

        FactoryRegistry registry =
                FactoryRegistry.discover(
                        effectiveClassLoader, pluginDirectories);

        ConnectorPreparer preparer =
                new ConnectorPreparer(
                        registry,
                        effectiveClassLoader);

        JobPlanner planner =
                new JobPlanner();

        return new LocalFluxEngine(
                effectiveClassLoader,
                preparer,
                planner,
                registry);
    }

    @Override
    public JobResult execute(
            JobDefinition definition)
            throws Exception {

        try {
            PreparedJob preparedJob = connectorPreparer.prepare(definition);
            ExecutionPlan executionPlan = jobPlanner.plan(preparedJob);
            return new JobExecution(executionPlan, classLoader).execute();
        } finally {
            registry.close();
        }
    }

    @Override
    public void close() {
        registry.close();
    }
}