package com.baize.flux.launcher;

import com.baize.flux.framework.execution.FluxEngine;
import com.baize.flux.framework.execution.LocalFluxEngine;
import com.baize.flux.framework.job.JobConfigParser;
import com.baize.flux.framework.job.JobDefinition;
import com.baize.flux.framework.job.JobResult;

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

    private LocalSyncLauncher() {
    }

    public static void main(String[] args)
            throws Exception {

        if (args.length != 1) {
            throw new IllegalArgumentException(
                    "Usage: LocalSyncLauncher "
                            + "<job.conf | \"HOCON job configuration\">");
        }

        run(
                readConfiguration(args[0]),
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

        JobResultPrinter.print(result);

        result.throwIfFailed();

        return result;
    }

    private static String readConfiguration(String argument) throws Exception {
        if (Files.isRegularFile(Paths.get(argument))) {
            return new String(Files.readAllBytes(Paths.get(argument)), StandardCharsets.UTF_8);
        }
        return argument;
    }
}
