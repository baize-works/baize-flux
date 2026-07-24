package com.baize.flux.api.dirtydata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LoggingDirtyDataCollector extends BoundedMemoryDirtyDataCollector {
    private static final Logger LOG = LoggerFactory.getLogger(LoggingDirtyDataCollector.class);

    public LoggingDirtyDataCollector(String task, int samples, long count, double percentage) {
        super(task, samples, count, percentage);
    }

    @Override
    public void collect(DirtyRecord record) {
        super.collect(record);
        LOG.warn("Dirty record: type={}, message={}, task={}", record.getErrorType(), record.getErrorMessage(), record.getContext().getTaskId());
    }
}
