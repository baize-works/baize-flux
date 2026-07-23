package com.baize.flux.launcher;

import com.baize.flux.api.configuration.ReadonlyConfig;
import com.baize.flux.api.source.RecordBatch;
import com.baize.flux.api.table.type.FluxRow;
import com.baize.flux.framework.channel.Channel;
import com.baize.flux.framework.channel.MemoryFluxChannel;
import com.baize.flux.framework.execution.source.SourceAction;
import com.baize.flux.framework.execution.source.SourceExecuteProcessor;
import com.baize.flux.framework.factory.PreparedSource;
import com.baize.flux.framework.util.FactoryUtil;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Runs one bounded source locally.
 *
 * <p>The sole argument is a HOCON string containing a {@code source} object
 * with {@code type}, optional {@code batch-size}, and connector options.
 */
public final class LocalSyncLauncher {

    private static final int DEFAULT_BATCH_SIZE = 1_000;

    private LocalSyncLauncher() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException(
                    "Usage: LocalSyncLauncher '<HOCON source configuration>'");
        }
        run(args[0], Thread.currentThread().getContextClassLoader());
    }

    /** Executes a HOCON source configuration and prints every emitted row. */
    public static void run(String hocon, ClassLoader classLoader) throws Exception {
        Config root = ConfigFactory.parseString(hocon).resolve();
        if (!root.hasPath("source")) {
            throw new IllegalArgumentException(
                    "HOCON configuration must contain a 'source' object");
        }

        Config sourceConfig = root.getConfig("source");
        if (!sourceConfig.hasPath("type")) {
            throw new IllegalArgumentException(
                    "HOCON configuration must contain 'source.type'");
        }

        String factoryIdentifier = sourceConfig.getString("type");
        int batchSize = sourceConfig.hasPath("batch-size")
                ? sourceConfig.getInt("batch-size")
                : DEFAULT_BATCH_SIZE;
        if (batchSize <= 0) {
            throw new IllegalArgumentException(
                    "source.batch-size must be greater than 0");
        }

        Config connectorConfig = sourceConfig.withoutPath("type")
                .withoutPath("batch-size");
        PreparedSource<?> preparedSource = FactoryUtil.createAndPrepareSource(
                factoryIdentifier,
                ReadonlyConfig.fromConfig(connectorConfig),
                classLoader);
        executeAndPrint(preparedSource, batchSize);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void executeAndPrint(
            PreparedSource<?> preparedSource, int batchSize) throws Exception {
        Channel<RecordBatch<FluxRow>> channel = new MemoryFluxChannel<>(1);
        AtomicReference<Throwable> producerFailure = new AtomicReference<>();

        Thread producer = new Thread(() -> {
            try {
                new SourceExecuteProcessor().execute(
                        new SourceAction("local-sync", (PreparedSource) preparedSource, batchSize),
                        batch -> channel.put(batch));
            } catch (Throwable failure) {
                producerFailure.set(failure);
            } finally {
                try {
                    channel.put(RecordBatch.endOfInput());
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "baize-flux-source-reader");

        producer.start();
        try {
            while (true) {
                RecordBatch<FluxRow> batch = channel.take();
                if (batch.isEndOfInput()) {
                    break;
                }
                for (FluxRow row : batch.getRecords()) {
                    System.out.println(row);
                }
            }
        } finally {
            producer.interrupt();
            producer.join();
        }

        Throwable failure = producerFailure.get();
        if (failure != null) {
            if (failure instanceof Exception) {
                throw (Exception) failure;
            }
            if (failure instanceof Error) {
                throw (Error) failure;
            }
            throw new RuntimeException(failure);
        }
    }
}
