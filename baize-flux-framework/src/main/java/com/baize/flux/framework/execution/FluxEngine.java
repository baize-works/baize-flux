package com.baize.flux.framework.execution;

import com.baize.flux.framework.job.JobDefinition;
import com.baize.flux.framework.job.JobResult;

/**
 * Flux 执行引擎。
 */
public interface FluxEngine extends AutoCloseable {

    JobResult execute(
            JobDefinition definition)
            throws Exception;

    @Override
    void close();
}