package com.baize.flux.connector.jdbc.core.split;

import java.util.List;
import java.util.Objects;

/**
 * Startup-time adaptive splitter. It caps planned chunks at the available reader parallelism;
 * scheduling remains the responsibility of the source enumerator.
 */
public final class DynamicChunkSplitter<T> implements ChunkSplitter<T> {
    private final ChunkSplitter<T> delegate;
    private final int maxChunks;
    public DynamicChunkSplitter(ChunkSplitter<T> delegate, int maxChunks) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
        if (maxChunks <= 0) throw new IllegalArgumentException("maxChunks must be greater than 0");
        this.maxChunks = maxChunks;
    }
    @Override public List<Chunk<T>> split(T lower, T upper, int requestedChunks) {
        if (requestedChunks <= 0) throw new IllegalArgumentException("chunkCount must be greater than 0");
        return delegate.split(lower, upper, Math.min(maxChunks, requestedChunks));
    }
}
