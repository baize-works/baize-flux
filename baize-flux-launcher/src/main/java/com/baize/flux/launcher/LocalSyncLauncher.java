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
 * 本地离线同步启动器。
 *
 * 未传入启动参数时使用内置 JDBC 测试配置；
 * 传入一个参数时，将该参数作为完整 HOCON 配置执行。
 */
public final class LocalSyncLauncher {

    private static final int DEFAULT_BATCH_SIZE = 1_000;

    /**
     * 本地测试配置。
     *
     * 使用前需要根据本地数据库修改：
     *
     * 1. 数据库地址；
     * 2. 用户名和密码；
     * 3. 表路径。
     */
    private static final String DEFAULT_HOCON =
            "source {\n"
                    + "  type = \"jdbc\"\n"
                    + "  batch-size = 100\n"
                    + "\n"
                    + "  url = \"jdbc:mysql://127.0.0.1:3306/flux_test"
                    + "?useSSL=false"
                    + "&allowPublicKeyRetrieval=true"
                    + "&serverTimezone=Asia/Shanghai"
                    + "&characterEncoding=UTF-8\"\n"
                    + "\n"
                    + "  driver = \"com.mysql.cj.jdbc.Driver\"\n"
                    + "  user = \"root\"\n"
                    + "  password = \"123456\"\n"
                    + "\n"
                    + "  table_path = \"flux_test.user_info\"\n"
                    + "  fetch_size = 100\n"
                    + "}\n";

    private LocalSyncLauncher() {
    }

    public static void main(String[] args) throws Exception {
        String hocon;

        if (args.length == 0) {
            hocon = DEFAULT_HOCON;

            System.out.println(
                    "No external HOCON configuration provided, "
                            + "use the built-in JDBC test configuration.");
        } else if (args.length == 1) {
            hocon = args[0];
        } else {
            throw new IllegalArgumentException(
                    "Usage: LocalSyncLauncher "
                            + "[\"<HOCON source configuration>\"]");
        }

        run(
                hocon,
                Thread.currentThread()
                        .getContextClassLoader());
    }

    /**
     * 执行 HOCON Source 配置。
     */
    public static void run(
            String hocon,
            ClassLoader classLoader)
            throws Exception {

        if (hocon == null || hocon.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "HOCON configuration must not be blank");
        }

        Config root =
                ConfigFactory.parseString(hocon)
                        .resolve();

        if (!root.hasPath("source")) {
            throw new IllegalArgumentException(
                    "HOCON configuration must contain "
                            + "a 'source' object");
        }

        Config sourceConfig =
                root.getConfig("source");

        if (!sourceConfig.hasPath("type")) {
            throw new IllegalArgumentException(
                    "HOCON configuration must contain "
                            + "'source.type'");
        }

        String factoryIdentifier =
                sourceConfig.getString("type")
                        .trim();

        if (factoryIdentifier.isEmpty()) {
            throw new IllegalArgumentException(
                    "source.type must not be blank");
        }

        int batchSize =
                sourceConfig.hasPath("batch-size")
                        ? sourceConfig.getInt("batch-size")
                        : DEFAULT_BATCH_SIZE;

        if (batchSize <= 0) {
            throw new IllegalArgumentException(
                    "source.batch-size must be greater than 0");
        }

        /*
         * type 和 batch-size 是 Framework 层配置，
         * 不应该继续传递给具体 Connector。
         */
        Config connectorConfig =
                sourceConfig
                        .withoutPath("type")
                        .withoutPath("batch-size");

        PreparedSource<?> preparedSource =
                FactoryUtil.createAndPrepareSource(
                        factoryIdentifier,
                        ReadonlyConfig.fromConfig(
                                connectorConfig),
                        classLoader);

        executeAndPrint(
                preparedSource,
                batchSize);
    }

    /**
     * 执行 Source，并打印读取到的数据。
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void executeAndPrint(
            PreparedSource<?> preparedSource,
            int batchSize)
            throws Exception {

        Channel<RecordBatch<FluxRow>> channel =
                new MemoryFluxChannel<>(1);

        AtomicReference<Throwable> producerFailure =
                new AtomicReference<>();

        Thread producer =
                new Thread(
                        () -> executeSource(
                                preparedSource,
                                batchSize,
                                channel,
                                producerFailure),
                        "baize-flux-source-reader");

        producer.start();

        try {
            consumeAndPrint(channel);
        } finally {
            if (producer.isAlive()) {
                producer.interrupt();
            }

            producer.join();
        }

        rethrowProducerFailure(
                producerFailure.get());
    }

    /**
     * Source 生产线程。
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void executeSource(
            PreparedSource<?> preparedSource,
            int batchSize,
            Channel<RecordBatch<FluxRow>> channel,
            AtomicReference<Throwable> producerFailure) {

        try {
            SourceAction action =
                    new SourceAction(
                            "local-sync",
                            (PreparedSource) preparedSource,
                            batchSize);

            new SourceExecuteProcessor()
                    .execute(
                            action,
                            channel::put);

        } catch (Throwable failure) {
            producerFailure.set(failure);

        } finally {
            try {
                channel.put(
                        RecordBatch.endOfInput());
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 消费并打印 Source 输出数据。
     */
    private static void consumeAndPrint(
            Channel<RecordBatch<FluxRow>> channel)
            throws InterruptedException {

        long totalRows = 0L;
        long batchCount = 0L;

        while (true) {
            RecordBatch<FluxRow> batch =
                    channel.take();

            if (batch.isEndOfInput()) {
                break;
            }

            batchCount++;

            for (FluxRow row : batch.getRecords()) {
                totalRows++;
                System.out.println(row);
            }
        }

        System.out.println(
                "Source execution finished, batches="
                        + batchCount
                        + ", rows="
                        + totalRows);
    }

    /**
     * 将生产线程异常重新抛到主线程。
     */
    private static void rethrowProducerFailure(
            Throwable failure)
            throws Exception {

        if (failure == null) {
            return;
        }

        if (failure instanceof Exception) {
            throw (Exception) failure;
        }

        if (failure instanceof Error) {
            throw (Error) failure;
        }

        throw new RuntimeException(failure);
    }
}