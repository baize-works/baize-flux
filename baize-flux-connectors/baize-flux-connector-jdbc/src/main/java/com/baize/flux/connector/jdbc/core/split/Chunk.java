package com.baize.flux.connector.jdbc.core.split;

import java.util.Objects;

/** A half-open key range; the last chunk may include its upper bound. */
public final class Chunk<T> {
    private final T start;
    private final T end;
    private final boolean endInclusive;

    public Chunk(T start, T end, boolean endInclusive) {
        this.start = Objects.requireNonNull(start, "start must not be null");
        this.end = Objects.requireNonNull(end, "end must not be null");
        this.endInclusive = endInclusive;
    }

    public T getStart() { return start; }
    public T getEnd() { return end; }
    public boolean isEndInclusive() { return endInclusive; }
}
