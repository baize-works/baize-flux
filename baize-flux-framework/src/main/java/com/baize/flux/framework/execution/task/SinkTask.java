package com.baize.flux.framework.execution.task;

import com.baize.flux.api.sink.SinkWriter;
import com.baize.flux.api.sink.SinkWriterContext;
import com.baize.flux.api.sink.SinkWriterMetrics;
import com.baize.flux.api.table.type.FluxRow;
import com.baize.flux.framework.channel.InputGate;
import com.baize.flux.framework.channel.RecordEnvelope;
import com.baize.flux.framework.execution.TaskContext;
import com.baize.flux.framework.execution.TaskId;
import com.baize.flux.framework.planner.SinkTaskPlan;

import java.util.Objects;
import java.util.concurrent.CancellationException;

/**
 * Sink 数据写入任务。
 */
public final class SinkTask
        implements ExecutionTask {

    private final SinkTaskPlan plan;

    private final InputGate<RecordEnvelope<FluxRow>>
            inputGate;

    public SinkTask(
            SinkTaskPlan plan,
            InputGate<RecordEnvelope<FluxRow>>
                    inputGate) {

        this.plan =
                Objects.requireNonNull(
                        plan,
                        "plan must not be null");

        this.inputGate =
                Objects.requireNonNull(
                        inputGate,
                        "inputGate must not be null");
    }

    @Override
    public TaskId getTaskId() {
        return plan.getTaskId();
    }

    @Override
    public void execute(
            TaskContext context)
            throws Exception {

        SinkWriter<FluxRow> writer = null;

        Throwable failure = null;

        boolean committed = false;

        try {
            writer = plan.getPreparedSink().getSink().createWriter(
                    new SinkWriterContext(getTaskId(), getTaskId().getSubtaskIndex(), getTaskId().getParallelism(),
                            context.getClassLoader(), new TaskMetricsAdapter(context),
                            plan.getPreparedSink().getPreparedTargetTables()));
            if (writer == null) {
                throw new IllegalStateException("Sink returned a null writer for " + getTaskId());
            }

            writer.open();

            while (true) {
                /*
                 * 不能因为其他 Task 触发取消，就正常退出并提交部分数据。
                 */
                if (context
                        .getCancellationToken()
                        .isCancelled()) {

                    throw new CancellationException(
                            "SinkTask was cancelled: "
                                    + getTaskId());
                }

                RecordEnvelope<FluxRow> envelope =
                        inputGate.read();

                /*
                 * 所有 SourceTask 均已结束。
                 */
                if (envelope == null) {
                    break;
                }

                long sqlStart = System.nanoTime();
                try {
                    writer.write(
                            envelope.getBatch(),
                            envelope.getCatalogTable());
                } finally {
                    context.getMetrics().addSqlExecutionNanos(
                            System.nanoTime() - sqlStart);
                }

                context.getMetrics().setCurrentPosition(
                        envelope.getBatch().getDataSetId(),
                        envelope.getBatch().getSplitId());

                context.getMetrics()
                        .incrementBatchCount();

                context.getMetrics()
                        .addSinkWriteSuccessRecords(
                                envelope
                                        .getBatch()
                                        .getRecords()
                                        .size());
            }

            /*
             * 在读取结束与提交之间再次检查取消状态，
             * 防止最后一批处理完成时其他任务刚好失败。
             */
            if (context
                    .getCancellationToken()
                    .isCancelled()) {

                throw new CancellationException(
                        "SinkTask was cancelled before commit: "
                                + getTaskId());
            }

            long commitStart = System.nanoTime();
            writer.commit();
            context.getMetrics().addDatabaseCommitNanos(System.nanoTime() - commitStart);

            committed = true;

        } catch (Throwable throwable) {
            failure = throwable;

            if (writer != null) {
                try {
                    writer.rollback();
                } catch (Throwable rollbackFailure) {
                    throwable.addSuppressed(
                            rollbackFailure);
                }
            }

            throw propagate(throwable);

        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Throwable closeFailure) {
                    if (failure != null) {
                        failure.addSuppressed(
                                closeFailure);

                    } else if (!committed) {
                        /*
                         * 事务尚未提交时，关闭异常需要让任务失败。
                         */
                        throw propagate(
                                closeFailure);
                    }

                    /*
                     * 数据已经提交后，单纯连接关闭异常不能再把任务标记为失败。
                     * 否则上层重试可能造成数据重复。
                     */
                }
            }
        }
    }

    /** 将框架指标以最小 API 暴露给 Connector。 */
    private static final class TaskMetricsAdapter implements SinkWriterMetrics {
        private final TaskContext context;
        private TaskMetricsAdapter(TaskContext context) { this.context = context; }
        @Override public void incrementWriteSuccessRecords(long count) { context.getMetrics().addSinkWriteSuccessRecords(count); }
        @Override public void addWrittenBytes(long count) { context.getMetrics().addSinkWrittenBytes(count); }
    }

    private static RuntimeException propagate(
            Throwable throwable)
            throws Exception {

        if (throwable instanceof Exception) {
            throw (Exception) throwable;
        }

        if (throwable instanceof Error) {
            throw (Error) throwable;
        }

        return new RuntimeException(
                throwable);
    }
}
