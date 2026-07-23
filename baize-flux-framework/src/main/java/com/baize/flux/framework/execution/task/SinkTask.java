package com.baize.flux.framework.execution.task;

import com.baize.flux.api.sink.SinkWriter;
import com.baize.flux.api.table.type.FluxRow;
import com.baize.flux.framework.channel.InputGate;
import com.baize.flux.framework.channel.RecordEnvelope;
import com.baize.flux.framework.execution.TaskContext;
import com.baize.flux.framework.execution.TaskId;
import com.baize.flux.framework.planner.SinkTaskPlan;

import java.util.Objects;

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
    public void execute(TaskContext context)
            throws Exception {

        SinkWriter<FluxRow> writer = null;

        Throwable failure = null;

        try {
            /*
             * 每个 SinkTask 创建自己的 Writer。
             */
            writer =
                    plan.getPreparedSink()
                            .createWriter();

            while (!context
                    .getCancellationToken()
                    .isCancelled()) {

                RecordEnvelope<FluxRow> envelope =
                        inputGate.read();

                /*
                 * Channel 所有生产者均已完成。
                 */
                if (envelope == null) {
                    break;
                }

                writer.write(
                        envelope.getBatch(),
                        envelope.getCatalogTable());

                context.getMetrics()
                        .incrementBatchCount();

                context.getMetrics()
                        .addRecordCount(
                                envelope.getBatch()
                                        .getRecords()
                                        .size());
            }

        } catch (Throwable throwable) {
            failure = throwable;

            throw propagate(throwable);

        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Throwable closeFailure) {
                    if (failure != null) {
                        failure.addSuppressed(
                                closeFailure);
                    } else {
                        throw propagate(
                                closeFailure);
                    }
                }
            }
        }
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