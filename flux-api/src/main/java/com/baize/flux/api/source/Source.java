package com.baize.flux.api.source;
import com.baize.flux.api.table.*; import java.util.*;
public interface Source { TableSchema producedSchema(); List<SourceSplit> planSplits(); SourceReader createReader(SourceSplit split); }
