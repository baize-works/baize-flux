package com.baize.flux.connector.jdbc.core.split;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ChunkSplitterTest {
    @Test
    public void fixedSplitterCreatesContiguousNumericRanges() {
        List<Chunk<BigDecimal>> chunks = new FixedChunkSplitter().split(new BigDecimal("0"), new BigDecimal("10"), 3);
        assertEquals(3, chunks.size());
        assertEquals(new BigDecimal("0"), chunks.get(0).getStart());
        assertEquals(chunks.get(0).getEnd(), chunks.get(1).getStart());
        assertEquals(new BigDecimal("10"), chunks.get(2).getEnd());
        assertTrue(chunks.get(2).isEndInclusive());
    }

    @Test
    public void asciiSplitterCreatesContiguousRanges() {
        List<Chunk<String>> chunks = new AsciiStringRangeSplitter().split("AA", "AZ", 4);
        assertEquals(4, chunks.size());
        assertEquals("AA", chunks.get(0).getStart());
        assertEquals(chunks.get(0).getEnd(), chunks.get(1).getStart());
        assertEquals("AZ", chunks.get(3).getEnd());
        assertTrue(chunks.get(3).isEndInclusive());
    }

    @Test
    public void dynamicSplitterCapsRequestedChunks() {
        List<Chunk<BigDecimal>> chunks = new DynamicChunkSplitter<BigDecimal>(new FixedChunkSplitter(), 2)
                .split(BigDecimal.ZERO, new BigDecimal("10"), 8);
        assertEquals(2, chunks.size());
    }
}
