package com.baize.flux.framework.execution;

/**
 * Metrics produced by one completed local job.
 */
public final class JobResult {
    private final long readRecords, writtenRecords, failedRecords, elapsedMillis;

    JobResult(long r, long w, long f, long e) {
        readRecords = r;
        writtenRecords = w;
        failedRecords = f;
        elapsedMillis = e;
    }

    public long readRecords() {
        return readRecords;
    }

    public long writtenRecords() {
        return writtenRecords;
    }

    public long failedRecords() {
        return failedRecords;
    }

    public long elapsedMillis() {
        return elapsedMillis;
    }

    public double recordsPerSecond() {
        return elapsedMillis == 0 ? writtenRecords : writtenRecords * 1000.0 / elapsedMillis;
    }
}
