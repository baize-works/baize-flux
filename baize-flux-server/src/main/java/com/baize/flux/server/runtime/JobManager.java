package com.baize.flux.server.runtime;

import com.baize.flux.framework.job.JobDefinition;

import java.util.List;

public interface JobManager
        extends AutoCloseable {

    JobSnapshot submit(JobDefinition definition);

    JobSnapshot getJob(String jobId);

    List<JobSnapshot> listJobs();

    JobSnapshot cancel(String jobId);

    boolean isClosed();

    void close();
}