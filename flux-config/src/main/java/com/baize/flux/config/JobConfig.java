package com.baize.flux.config;
import java.util.*;
/** Immutable configuration for one bounded source-to-sink job. */
public record JobConfig(String jobName, ConnectorConfig source, List<TransformConfig> transforms, ConnectorConfig sink, int batchSize) {
 public JobConfig { Objects.requireNonNull(jobName); Objects.requireNonNull(source); transforms=List.copyOf(transforms); Objects.requireNonNull(sink); if(batchSize<1) throw new IllegalArgumentException("batchSize must be positive"); }
 public record ConnectorConfig(String identifier, Map<String,String> options) { public ConnectorConfig { Objects.requireNonNull(identifier); options=Map.copyOf(options); } public String required(String key) { var value=options.get(key); if(value==null || value.isBlank()) throw new IllegalArgumentException("Missing required option: "+key); return value; } }
 public record TransformConfig(String identifier, Map<String,String> options) { public TransformConfig { options=Map.copyOf(options); } }
}
