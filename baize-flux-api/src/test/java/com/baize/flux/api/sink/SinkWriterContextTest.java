package com.baize.flux.api.sink;

import com.baize.flux.api.table.catalog.CatalogTable;
import com.baize.flux.api.table.catalog.TablePath;
import java.util.Collections;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class SinkWriterContextTest {
    @Test
    public void exposesTaskIdentityParallelismAndClassLoader() {
        ClassLoader loader = getClass().getClassLoader();
        SinkWriterMetrics metrics = new SinkWriterMetrics() {
            @Override public void incrementWriteSuccessRecords(long count) { }
            @Override public void addWrittenBytes(long count) { }
        };
        Map<TablePath, CatalogTable> tables = Collections.emptyMap();
        SinkWriterContext context = new SinkWriterContext(new TaskId("sink", 1, 3), 1, 3,
                loader, metrics, tables);
        assertEquals(1, context.getSubtaskIndex());
        assertEquals(3, context.getParallelism());
        assertEquals("sink", context.getTaskId().getStageName());
        assertSame(loader, context.getClassLoader());
        assertSame(metrics, context.getMetrics());
        assertEquals(tables, context.getPreparedTargetTables());
    }
}
