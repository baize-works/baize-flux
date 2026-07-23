package com.baize.flux.connector.jdbc.core.split;

import java.util.List;
import java.util.Objects;

/**
 * Startup-time splitter that caps a table's requested chunks at the available reader
 * parallelism. The delegate owns range-boundary calculation while this class owns the
 * scheduling constraint.
 */
public final class DynamicChunkSplitter<T> implements ChunkSplitter<T> {
    private final ChunkSplitter<T> delegate;
    private final int maxChunks;
    public DynamicChunkSplitter(ChunkSplitter<T> delegate, int maxChunks) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
        if (maxChunks <= 0) throw new IllegalArgumentException("maxChunks must be greater than 0");
        this.maxChunks = maxChunks;
    }

    @Override
    public List<Chunk<T>> split(T lower, T upper, int requestedChunks) {
        return delegate.split(lower, upper, effectiveChunkCount(requestedChunks, maxChunks));
    }

    /**
     * Resolves the number of chunks that may be planned for one table. Kept package-visible so
     * the planner and tests share the same validation and capping semantics.
     */
    static int effectiveChunkCount(int requestedChunks, int parallelism) {
        if (requestedChunks <= 0) {
            throw new IllegalArgumentException("requestedChunks must be greater than 0");
        }
        if (parallelism <= 0) {
            throw new IllegalArgumentException("parallelism must be greater than 0");
        }
        return Math.min(requestedChunks, parallelism);
    }
}
