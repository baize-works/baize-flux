package com.baize.flux.launcher;

import com.baize.flux.framework.execution.FluxEngine;
import com.baize.flux.framework.execution.LocalFluxEngine;
import com.baize.flux.framework.job.JobConfigParser;
import com.baize.flux.framework.job.JobDefinition;
import com.baize.flux.framework.job.JobResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 本地离线同步启动器。
 *
 * <p>Launcher 只负责：
 *
 * <ol>
 *     <li>解析配置</li>
 *     <li>创建 Engine</li>
 *     <li>提交 Job</li>
 *     <li>输出执行结果</li>
 * </ol>
 */
public final class LocalSyncLauncher {

    private static final Logger LOG = LogManager.getLogger(LocalSyncLauncher.class);

    private LocalSyncLauncher() {
    }

    public static void main(String[] args)
            throws Exception {

        if (args.length == 1 && ("--help".equals(args[0]) || "-h".equals(args[0]))) {
            printUsage();
            return;
        }
        if (args.length == 1 && "--version".equals(args[0])) {
            System.out.println("Baize Flux 1.0.0");
            return;
        }
        String configuration;
        if (args.length == 1) {
            configuration = args[0];
        } else if (args.length == 2 && ("--config".equals(args[0]) || "-c".equals(args[0]))) {
            configuration = args[1];
        } else {
            printUsage();
            throw new IllegalArgumentException("A job configuration is required");
        }

        LOG.info("Starting Baize Flux job from {}", configuration);
        run(
                readConfiguration(configuration),
                Thread.currentThread()
                        .getContextClassLoader());
    }

    public static JobResult run(
            String hocon,
            ClassLoader classLoader)
            throws Exception {

        JobDefinition definition =
                new JobConfigParser()
                        .parse(hocon);

        JobResult result;

        try (FluxEngine engine =
                     LocalFluxEngine.create(
                             classLoader)) {

            result =
                    engine.execute(
                            definition);
        }

        JobResultPrinter.print(
                result,
                System.out,
                JobResultPrinter.DetailLevel.FULL);

        result.throwIfFailed();

        return result;
    }

    private static String readConfiguration(String argument) throws Exception {
        if (Files.isRegularFile(Paths.get(argument))) {
            return new String(Files.readAllBytes(Paths.get(argument)), StandardCharsets.UTF_8);
        }
        return argument;
    }

    private static void printUsage() {
        System.out.println("Usage: baize-flux [--config|-c] <job.conf|job.yaml>\n"
                + "       baize-flux --version\n       baize-flux --help\n\n"
                + "The job file uses HOCON syntax; the .yaml extension is supported for deployment conventions.");
    }
}
