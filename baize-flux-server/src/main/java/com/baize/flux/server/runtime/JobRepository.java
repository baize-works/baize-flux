package com.baize.flux.server.runtime; import java.util.*; public interface JobRepository { JobSnapshot get(String jobId); List<JobSnapshot> list(); }
