package com.baize.flux.connector.jdbc.core.split;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Deterministically divides a numeric range into equally sized chunks. */
public final class FixedChunkSplitter implements ChunkSplitter<BigDecimal> {
    @Override
    public List<Chunk<BigDecimal>> split(BigDecimal lower, BigDecimal upper, int count) {
        if (lower == null || upper == null || lower.compareTo(upper) > 0) {
            throw new IllegalArgumentException("numeric bounds must be non-null and ordered");
        }
        if (count <= 0) throw new IllegalArgumentException("chunkCount must be greater than 0");
        if (lower.compareTo(upper) == 0) return Collections.singletonList(new Chunk<>(lower, upper, true));
        BigDecimal width = upper.subtract(lower).divide(BigDecimal.valueOf(count), 20, RoundingMode.CEILING);
        List<Chunk<BigDecimal>> result = new ArrayList<>();
        BigDecimal start = lower;
        for (int index = 0; index < count && start.compareTo(upper) < 0; index++) {
            BigDecimal end = index == count - 1 ? upper : start.add(width).min(upper);
            result.add(new Chunk<>(start, end, end.compareTo(upper) == 0));
            start = end;
        }
        return Collections.unmodifiableList(result);
    }
}
