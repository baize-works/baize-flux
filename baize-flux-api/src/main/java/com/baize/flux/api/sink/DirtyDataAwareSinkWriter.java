package com.baize.flux.api.sink;
import com.baize.flux.api.dirtydata.DirtyDataContext;
import com.baize.flux.api.dirtydata.DirtyDataSummary;
/** Optional sink extension for task-scoped dirty-data reporting. */
public interface DirtyDataAwareSinkWriter { void configureDirtyData(DirtyDataContext context) throws Exception; DirtyDataSummary getDirtyDataSummary(); }
