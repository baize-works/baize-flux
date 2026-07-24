package com.baize.flux.api.source;

import com.baize.flux.api.table.type.FluxRow;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.Assert;
import org.junit.Test;

public class RecordSizeEstimatorTest {
    @Test public void estimatesSupportedFluxRowValuesAndBatch() {
        FluxRow row = FluxRow.of(null, 1, true, "text", new byte[] {1, 2}, new BigDecimal("1.23"), Instant.now(), new Object());
        long rowBytes = row.estimatedSizeBytes();
        Assert.assertTrue(rowBytes > RecordSizeEstimator.UNKNOWN_RECORD_BYTES);
        RecordBatch<FluxRow> batch = RecordBatch.of(split(), java.util.Collections.singletonList(row));
        Assert.assertEquals(1, batch.size());
        Assert.assertTrue(batch.getEstimatedBytes() >= rowBytes);
    }
    private static SourceSplit split() { return new SourceSplit() { public String splitId() { return "split"; } public String dataSetId() { return "data"; } }; }
}
