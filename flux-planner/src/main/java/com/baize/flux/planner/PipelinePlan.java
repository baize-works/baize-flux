package com.baize.flux.planner; import com.baize.flux.api.source.Source; import com.baize.flux.api.sink.Sink; import com.baize.flux.api.transform.Transform; import java.util.*;
/** Physical, local execution plan for a single bounded pipeline. */ public record PipelinePlan(Source source,List<Transform> transforms,Sink sink,int batchSize){public PipelinePlan{transforms=List.copyOf(transforms);}}
