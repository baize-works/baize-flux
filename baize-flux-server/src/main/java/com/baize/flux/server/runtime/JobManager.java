package com.baize.flux.server.runtime; import com.baize.flux.framework.job.JobDefinition; import java.util.List;
public interface JobManager extends AutoCloseable { String submit(JobDefinition definition); JobSnapshot getJob(String jobId); List<JobSnapshot> listJobs(); boolean cancel(String jobId); void close(); }
