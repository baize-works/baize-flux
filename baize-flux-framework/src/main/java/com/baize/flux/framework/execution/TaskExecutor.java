package com.baize.flux.framework.execution;

import com.baize.flux.framework.execution.task.ExecutionTask;
import com.baize.flux.framework.metrics.TaskMetrics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 本地 Task 线程执行器。
 */
public final class TaskExecutor
        implements AutoCloseable {

    private static final Logger LOG = LogManager.getLogger(TaskExecutor.class);

    private final ExecutorService executorService;

    private final ExecutorCompletionService<TaskResult>
            completionService;

    private final String jobName;

    private final long runId;

    public TaskExecutor(
            int threadCount,
            final String threadPrefix) {

        this(threadCount, threadPrefix, "unnamed", System.currentTimeMillis());
    }

    public TaskExecutor(
            int threadCount,
            final String threadPrefix,
            String jobName,
            long runId) {

        if (threadCount <= 0) {
            throw new IllegalArgumentException(
                    "threadCount must be greater than 0");
        }

        this.jobName = Objects.requireNonNull(jobName, "jobName must not be null");
        if (runId < 0) {
            throw new IllegalArgumentException("runId must not be negative");
        }
        this.runId = runId;

        final AtomicInteger sequence =
                new AtomicInteger();

        ThreadFactory threadFactory =
                new ThreadFactory() {
                    @Override
                    public Thread newThread(
                            Runnable runnable) {

                        Thread thread =
                                new Thread(
                                        runnable,
                                        threadPrefix
                                                + "-"
                                                + sequence
                                                .getAndIncrement());

                        thread.setDaemon(false);

                        return thread;
                    }
                };

        this.executorService =
                Executors.newFixedThreadPool(
                        threadCount,
                        threadFactory);

        this.completionService =
                new ExecutorCompletionService<TaskResult>(
                        executorService);
    }

    public Future<TaskResult> submit(
            final ExecutionTask task,
            final TaskContext context) {

        Objects.requireNonNull(
                task,
                "task must not be null");

        Objects.requireNonNull(
                context,
                "context must not be null");

        return completionService.submit(
                new Callable<TaskResult>() {
                    @Override
                    public TaskResult call() {

                        String taskLogFile = TaskLogFileName.create(jobName, task.getTaskId(), runId);
                        ThreadContext.put("taskLogFile", taskLogFile);

                        TaskMetrics metrics =
                                context.getMetrics();

                        if (context
                                .getCancellationToken()
                                .isCancelled()) {

                            metrics.markFinished(
                                    TaskState.CANCELED);

                            LOG.info("Task cancelled before execution: {}", task.getTaskId());
                            ThreadContext.remove("taskLogFile");
                            return TaskResult.canceled(
                                    task.getTaskId(),
                                    context
                                            .getCancellationToken()
                                            .getCause());
                        }

                        metrics.markStarted();
                        LOG.info("Task started: {}", task.getTaskId());

                        try {
                            task.execute(context);

                            if (context
                                    .getCancellationToken()
                                    .isCancelled()) {

                                metrics.markFinished(
                                        TaskState.CANCELED);

                                LOG.info("Task cancelled: {}", task.getTaskId());
                                return TaskResult.canceled(
                                        task.getTaskId(),
                                        context
                                                .getCancellationToken()
                                                .getCause());
                            }

                            metrics.markFinished(
                                    TaskState.FINISHED);

                            LOG.info("Task finished: {}", task.getTaskId());
                            return TaskResult.finished(
                                    task.getTaskId());

                        } catch (Throwable throwable) {
                            if (context
                                    .getCancellationToken()
                                    .isCancelled()
                                    && throwable
                                    instanceof InterruptedException) {

                                Thread.currentThread()
                                        .interrupt();

                                metrics.markFinished(
                                        TaskState.CANCELED);

                                LOG.info("Task cancelled: {}", task.getTaskId());
                                return TaskResult.canceled(
                                        task.getTaskId(),
                                        throwable);
                            }

                            metrics.markFinished(
                                    TaskState.FAILED);

                            LOG.error("Task failed: " + task.getTaskId(), throwable);

                            return TaskResult.failed(
                                    task.getTaskId(),
                                    throwable);
                        } finally {
                            ThreadContext.remove("taskLogFile");
                        }
                    }
                });
    }

    public Future<TaskResult> takeCompleted()
            throws InterruptedException {

        return completionService.take();
    }

    @Override
    public void close() {
        executorService.shutdownNow();

        try {
            if (!executorService.awaitTermination(
                    5L,
                    TimeUnit.SECONDS)) {

                executorService.shutdownNow();
            }
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            executorService.shutdownNow();
        }
    }
}
