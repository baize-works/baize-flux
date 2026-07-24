package com.baize.flux.framework.connector;

import com.baize.flux.api.configuration.ReadonlyConfig;
import com.baize.flux.api.sink.Sink;
import com.baize.flux.api.sink.SinkWriter;
import com.baize.flux.api.sink.SinkWriterContext;
import com.baize.flux.api.source.RecordBatch;
import com.baize.flux.api.table.catalog.CatalogTable;
import com.baize.flux.api.table.type.FluxRow;
import org.junit.Test;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class PreparedSinkTest {
    @Test
    public void storesSinkAndDoesNotCreateWriterDuringPreparation() {
        AtomicInteger writerCreates = new AtomicInteger();
        Sink sink = new Sink() {
            @Override public SinkWriter<FluxRow> createWriter(SinkWriterContext context) {
                writerCreates.incrementAndGet(); return new NoOpSinkWriter();
            }
        };
        PreparedSink preparedSink = new PreparedSink("test",
                ReadonlyConfig.fromMap(Collections.<String, Object>emptyMap()), sink,
                Collections.emptyMap());
        assertSame(sink, preparedSink.getSink());
        assertEquals(0, writerCreates.get());
    }
    private static final class NoOpSinkWriter implements SinkWriter<FluxRow> {
        @Override public void write(RecordBatch<FluxRow> batch, CatalogTable sourceTable) { }
        @Override public void close() { }
    }
}
