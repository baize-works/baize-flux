package com.baize.flux.connector.jdbc.core.split;

import java.util.List;

/** Splits an already known key range into non-overlapping chunks. */
public interface ChunkSplitter<T> {
    List<Chunk<T>> split(T lowerBound, T upperBound, int chunkCount);
}
