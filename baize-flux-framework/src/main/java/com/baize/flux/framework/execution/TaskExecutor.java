package com.baize.flux.framework.execution;

import com.baize.flux.framework.execution.task.ExecutionTask;
import com.baize.flux.framework.metrics.TaskMetrics;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 本地 Task 线程执行器。
 */
public final class TaskExecutor
        implements AutoCloseable {

    private final ExecutorService executorService;

    private final ExecutorCompletionService<TaskResult>
            completionService;

    public TaskExecutor(
            int threadCount,
            final String threadPrefix) {

        if (threadCount <= 0) {
            throw new IllegalArgumentException(
                    "threadCount must be greater than 0");
        }

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

                        TaskMetrics metrics =
                                context.getMetrics();

                        if (context
                                .getCancellationToken()
                                .isCancelled()) {

                            metrics.markFinished(
                                    TaskState.CANCELED);

                            return TaskResult.canceled(
                                    task.getTaskId(),
                                    context
                                            .getCancellationToken()
                                            .getCause());
                        }

                        metrics.markStarted();

                        try {
                            task.execute(context);

                            if (context
                                    .getCancellationToken()
                                    .isCancelled()) {

                                metrics.markFinished(
                                        TaskState.CANCELED);

                                return TaskResult.canceled(
                                        task.getTaskId(),
                                        context
                                                .getCancellationToken()
                                                .getCause());
                            }

                            metrics.markFinished(
                                    TaskState.FINISHED);

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

                                return TaskResult.canceled(
                                        task.getTaskId(),
                                        throwable);
                            }

                            metrics.markFinished(
                                    TaskState.FAILED);

                            return TaskResult.failed(
                                    task.getTaskId(),
                                    throwable);
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