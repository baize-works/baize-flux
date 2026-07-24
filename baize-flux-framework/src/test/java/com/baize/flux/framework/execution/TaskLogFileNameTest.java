package com.baize.flux.framework.execution;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TaskLogFileNameTest {

    @Test
    public void createsSafePerTaskFileName() {
        assertEquals(
                "job-orders_sync-pipeline_source.orders-source-0-128392984.log",
                TaskLogFileName.create("orders sync", new TaskId("pipeline/source.orders", TaskType.SOURCE, 0, 1), 128392984L));
    }
}
