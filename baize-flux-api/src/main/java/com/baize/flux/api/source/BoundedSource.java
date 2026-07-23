package com.baize.flux.api.source;
import java.util.List;
/** A finite source. First runtime version supports exactly one planned split. */
public interface BoundedSource<T, SplitT extends SourceSplit> {
    List<SplitT> planSplits(int parallelism) throws Exception;
    SourceReader<T, SplitT> createReader() throws Exception;
}
