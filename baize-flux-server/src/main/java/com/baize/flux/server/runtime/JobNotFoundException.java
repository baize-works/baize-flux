package com.baize.flux.server.runtime;

public final class JobNotFoundException
        extends RuntimeException {

    public JobNotFoundException(String jobId) {
        super("Job not found: " + jobId);
    }
}