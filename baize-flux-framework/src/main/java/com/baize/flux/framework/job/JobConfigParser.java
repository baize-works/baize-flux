package com.baize.flux.framework.job;

import com.baize.flux.api.configuration.ReadonlyConfig;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigResolveOptions;

/**
 * HOCON Job 配置解析器。
 */
public final class JobConfigParser {

    public JobDefinition parse(String hocon) {
        if (hocon == null || hocon.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "HOCON configuration must not be blank");
        }

        /*
         * Connector 中的 ${schema_name}、${table_name}
         * 属于 Connector 占位符，不应该由 HOCON 提前解析。
         */
        Config root =
                ConfigFactory.parseString(hocon)
                        .resolve(
                                ConfigResolveOptions.defaults()
                                        .setAllowUnresolved(true));

        requireObject(root, "source");
        requireObject(root, "sink");

        Config sourceConfig =
                root.getConfig("source");

        Config sinkConfig =
                root.getConfig("sink");

        String jobName =
                root.hasPath("job.name")
                        ? root.getString("job.name")
                        : "local-sync";

        String sourceType =
                requireString(
                        sourceConfig,
                        "type",
                        "source.type");

        String sinkType =
                requireString(
                        sinkConfig,
                        "type",
                        "sink.type");

        int batchSize =
                sourceConfig.hasPath("batch-size")
                        ? sourceConfig.getInt("batch-size")
                        : ExecutionConfig.DEFAULT_BATCH_SIZE;

        int sourceParallelism =
                readSourceParallelism(root);

        int sinkParallelism =
                root.hasPath("env.sink-parallelism")
                        ? root.getInt("env.sink-parallelism")
                        : ExecutionConfig.DEFAULT_SINK_PARALLELISM;

        int maxBufferedBatches = root.hasPath("env.max-buffered-batches")
                ? root.getInt("env.max-buffered-batches")
                : (root.hasPath("env.channel-capacity") ? root.getInt("env.channel-capacity") : ExecutionConfig.DEFAULT_CHANNEL_CAPACITY);
        long maxBufferedRecords = readLong(root, "env.max-buffered-records");
        long maxBufferedBytes = readLong(root, "env.max-buffered-bytes");
        long maxRecordsPerSecond = readLong(root, "env.max-records-per-second");
        long maxBytesPerSecond = readLong(root, "env.max-bytes-per-second");

        Config sourceConnectorConfig =
                sourceConfig
                        .withoutPath("type")
                        .withoutPath("batch-size");

        Config sinkConnectorConfig =
                sinkConfig.withoutPath("type");

        SourceDefinition source =
                new SourceDefinition(
                        sourceType,
                        ReadonlyConfig.fromConfig(
                                sourceConnectorConfig));

        SinkDefinition sink =
                new SinkDefinition(
                        sinkType,
                        ReadonlyConfig.fromConfig(
                                sinkConnectorConfig));

        ExecutionConfig executionConfig =
                new ExecutionConfig(
                        batchSize,
                        sourceParallelism,
                        sinkParallelism,
                        maxBufferedBatches,
                        maxBufferedRecords,
                        maxBufferedBytes,
                        maxRecordsPerSecond,
                        maxBytesPerSecond);

        return new JobDefinition(
                jobName,
                source,
                sink,
                executionConfig);
    }

    private static long readLong(Config root, String path) {
        return root.hasPath(path) ? root.getLong(path) : 0L;
    }

    private int readSourceParallelism(Config root) {
        if (root.hasPath("env.source-parallelism")) {
            return root.getInt(
                    "env.source-parallelism");
        }

        /*
         * 兼容旧配置 env.parallelism。
         */
        if (root.hasPath("env.parallelism")) {
            return root.getInt(
                    "env.parallelism");
        }

        return ExecutionConfig.DEFAULT_SOURCE_PARALLELISM;
    }

    private static void requireObject(
            Config root,
            String path) {

        if (!root.hasPath(path)) {
            throw new IllegalArgumentException(
                    "HOCON configuration must contain '"
                            + path
                            + "' object");
        }
    }

    private static String requireString(
            Config config,
            String path,
            String fullPath) {

        if (!config.hasPath(path)) {
            throw new IllegalArgumentException(
                    "HOCON configuration must contain '"
                            + fullPath
                            + "'");
        }

        String value =
                config.getString(path).trim();

        if (value.isEmpty()) {
            throw new IllegalArgumentException(
                    fullPath + " must not be blank");
        }

        return value;
    }
}