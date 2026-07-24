package com.baize.flux.server.runtime;

import com.baize.flux.framework.execution.JobExecutionListener;
import com.baize.flux.framework.job.JobDefinition;
import com.baize.flux.framework.job.JobResult;

/**
 * 将 JobManager 与具体执行引擎解耦，便于单元测试。
 */
public interface JobExecutor {

    JobResult execute(
            JobDefinition definition,
            JobExecutionListener listener)
            throws Exception;
}