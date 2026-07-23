package com.baize.flux.launcher;

import com.baize.flux.api.configuration.*;
import com.baize.flux.api.job.JobDefinition;
import com.baize.flux.framework.configuration.HoconConfigLoader;
import com.baize.flux.framework.planner.JobPlanner;
import com.baize.flux.framework.plugin.FactoryRegistry;

import java.nio.file.Paths;
import java.util.Map;

/**
 * Command-line entry point for one local bounded sync job.
 */
public final class LauncherConfigurationExample {
    private static final Option<String> NAME = Options.key("job.name").stringType().noDefaultValue();
    private static final Option<Integer> BATCH_SIZE = Options.key("runtime.batch-size").intType().defaultValue(1000);
    private static final Option<String> SOURCE_TYPE = Options.key("source.type").stringType().noDefaultValue();
    private static final Option<Map<String, Object>> SOURCE_OPTIONS = Options.key("source.options").mapObjectType().noDefaultValue();
    private static final Option<String> SINK_TYPE = Options.key("sink.type").stringType().noDefaultValue();
    private static final Option<Map<String, Object>> SINK_OPTIONS = Options.key("sink.options").mapObjectType().noDefaultValue();
    private static final OptionRule JOB_RULE = OptionRule.builder().required(NAME, SOURCE_TYPE, SOURCE_OPTIONS, SINK_TYPE, SINK_OPTIONS).optional(BATCH_SIZE).constrain(NAME, Constraints.notBlank()).constrain(SOURCE_TYPE, Constraints.notBlank()).constrain(SINK_TYPE, Constraints.notBlank()).constrain(BATCH_SIZE, Constraints.greaterOrEqual(1)).build();

    private LauncherConfigurationExample() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Usage: LauncherConfigurationExample <job.conf>");
        ReadonlyConfig config = new HoconConfigLoader().load(Paths.get(args[0]));
        JobResult result = new LocalJobExecutor().execute(new JobPlanner(FactoryRegistry.discover()).plan(toJobDefinition(config)));
        System.out.println("Job completed: read=" + result.readRecords() + ", written=" + result.writtenRecords() + ", failed=" + result.failedRecords() + ", elapsedMs=" + result.elapsedMillis() + ", recordsPerSecond=" + result.recordsPerSecond());
    }

    public static JobDefinition toJobDefinition(ReadonlyConfig config) {
        ConfigValidator.strict().validate(config, JOB_RULE).throwIfInvalid();
        return new JobDefinition(config.get(NAME), config.get(BATCH_SIZE), config.get(SOURCE_TYPE), ReadonlyConfig.fromMap(config.get(SOURCE_OPTIONS)), config.get(SINK_TYPE), ReadonlyConfig.fromMap(config.get(SINK_OPTIONS)));
    }
}
