package com.baize.flux.framework.execution;

import com.baize.flux.framework.connector.ConnectorPreparer;
import com.baize.flux.framework.connector.FactoryRegistry;
import com.baize.flux.framework.connector.PreparedJob;
import com.baize.flux.framework.job.JobDefinition;
import com.baize.flux.framework.job.JobResult;
import com.baize.flux.framework.planner.ExecutionPlan;
import com.baize.flux.framework.planner.JobPlanner;

import java.util.Objects;

/**
 * 本地离线 Flux 执行引擎。
 */
public final class LocalFluxEngine
        implements FluxEngine {

    private final ClassLoader classLoader;

    private final ConnectorPreparer connectorPreparer;

    private final JobPlanner jobPlanner;

    public LocalFluxEngine(
            ClassLoader classLoader,
            ConnectorPreparer connectorPreparer,
            JobPlanner jobPlanner) {

        this.classLoader =
                Objects.requireNonNull(
                        classLoader,
                        "classLoader must not be null");

        this.connectorPreparer =
                Objects.requireNonNull(
                        connectorPreparer,
                        "connectorPreparer must not be null");

        this.jobPlanner =
                Objects.requireNonNull(
                        jobPlanner,
                        "jobPlanner must not be null");
    }

    public static LocalFluxEngine create(
            ClassLoader classLoader) {

        ClassLoader effectiveClassLoader =
                classLoader == null
                        ? Thread.currentThread()
                        .getContextClassLoader()
                        : classLoader;

        FactoryRegistry registry =
                FactoryRegistry.discover(
                        effectiveClassLoader);

        ConnectorPreparer preparer =
                new ConnectorPreparer(
                        registry,
                        effectiveClassLoader);

        JobPlanner planner =
                new JobPlanner();

        return new LocalFluxEngine(
                effectiveClassLoader,
                preparer,
                planner);
    }

    @Override
    public JobResult execute(
            JobDefinition definition)
            throws Exception {

        PreparedJob preparedJob =
                connectorPreparer.prepare(
                        definition);

        ExecutionPlan executionPlan =
                jobPlanner.plan(
                        preparedJob);

        JobExecution jobExecution =
                new JobExecution(
                        executionPlan,
                        classLoader);

        return jobExecution.execute();
    }

    @Override
    public void close() {
        /*
         * 当前 Engine 不持有长生命周期线程池。
         * 后续支持多 Job 并发时，可在这里关闭资源。
         */
    }
}