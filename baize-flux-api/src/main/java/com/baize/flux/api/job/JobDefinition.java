package com.baize.flux.api.job;
import com.baize.flux.api.configuration.ReadonlyConfig;
/** Validated declarative definition of a single source-to-sink job. */
public final class JobDefinition {
    private final String name; private final Boundedness boundedness; private final int batchSize; private final String sourceType; private final ReadonlyConfig sourceOptions; private final String sinkType; private final ReadonlyConfig sinkOptions;
    public JobDefinition(String name, Boundedness boundedness, int batchSize, String sourceType, ReadonlyConfig sourceOptions, String sinkType, ReadonlyConfig sinkOptions) { this.name=name; this.boundedness=boundedness; this.batchSize=batchSize; this.sourceType=sourceType; this.sourceOptions=sourceOptions; this.sinkType=sinkType; this.sinkOptions=sinkOptions; }
    public String name(){return name;} public Boundedness boundedness(){return boundedness;} public int batchSize(){return batchSize;} public String sourceType(){return sourceType;} public ReadonlyConfig sourceOptions(){return sourceOptions;} public String sinkType(){return sinkType;} public ReadonlyConfig sinkOptions(){return sinkOptions;}
}
