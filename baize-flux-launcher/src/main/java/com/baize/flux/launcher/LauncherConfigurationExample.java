package com.baize.flux.launcher;

import com.baize.flux.api.configuration.*;

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

    private LauncherConfigurationExample() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Usage: LauncherConfigurationExample <job.conf>");
    }


}
