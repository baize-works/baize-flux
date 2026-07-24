package com.baize.flux.framework.channel;

import com.baize.flux.api.source.RecordBatch;
import com.baize.flux.api.source.SourceSplit;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;

public class LocalDataChannelCapacityTest {
    @Test public void releasesRecordAndByteCapacityAfterRead() throws Exception {
        RecordBatch<String> batch = RecordBatch.of(split(), Collections.singletonList("data"));
        LocalDataChannel<RecordBatch<String>> channel = new LocalDataChannel<RecordBatch<String>>("test", 1, 1, batch.getEstimatedBytes(), 0, 0, 1);
        ChannelWriter<RecordBatch<String>> writer = channel.openWriter();
        ChannelReader<RecordBatch<String>> reader = channel.openReader();
        writer.write(batch);
        Assert.assertEquals(1, channel.getMetrics().getCurrentRecords());
        Assert.assertEquals(batch.getEstimatedBytes(), channel.getMetrics().getCurrentBytes());
        Assert.assertSame(batch, reader.read());
        Assert.assertEquals(0, channel.getMetrics().getCurrentRecords());
        Assert.assertEquals(0, channel.getMetrics().getCurrentBytes());
        writer.close();
    }
    @Test public void permitsOversizedBatchOnlyWhenEmpty() throws Exception {
        RecordBatch<String> batch = RecordBatch.of(split(), Collections.singletonList("too large"));
        LocalDataChannel<RecordBatch<String>> channel = new LocalDataChannel<RecordBatch<String>>("test", 1, 0, 1, 0, 0, 1);
        ChannelWriter<RecordBatch<String>> writer = channel.openWriter();
        writer.write(batch);
        Assert.assertEquals(1, channel.getMetrics().getOversizedBatches());
        writer.close();
    }
    private static SourceSplit split() { return new SourceSplit() { public String splitId() { return "split"; } public String dataSetId() { return "data"; } }; }
}
