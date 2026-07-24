package com.baize.flux.framework.execution.task;

import com.baize.flux.api.source.RecordBatch;
import com.baize.flux.api.source.SourceReader;
import com.baize.flux.api.source.SourceSplit;
import com.baize.flux.api.table.catalog.CatalogTable;
import com.baize.flux.api.table.catalog.TablePath;
import com.baize.flux.api.table.type.FluxRow;
import com.baize.flux.framework.channel.OutputGate;
import com.baize.flux.framework.channel.RecordEnvelope;
import com.baize.flux.framework.connector.PreparedSource;
import com.baize.flux.framework.execution.TaskContext;
import com.baize.flux.framework.execution.TaskId;
import com.baize.flux.framework.execution.split.LocalSplitQueue;
import com.baize.flux.framework.planner.SourceTaskPlan;

import java.util.Map;
import java.util.Objects;

/**
 * Source 数据读取任务。
 */
public final class SourceTask<
        SplitT extends SourceSplit>
        implements ExecutionTask {

    private final SourceTaskPlan<SplitT> plan;

    private final OutputGate<RecordEnvelope<FluxRow>>
            outputGate;

    public SourceTask(
            SourceTaskPlan<SplitT> plan,
            OutputGate<RecordEnvelope<FluxRow>>
                    outputGate) {

        this.plan =
                Objects.requireNonNull(
                        plan,
                        "plan must not be null");

        this.outputGate =
                Objects.requireNonNull(
                        outputGate,
                        "outputGate must not be null");
    }

    @Override
    public TaskId getTaskId() {
        return plan.getTaskId();
    }

    @Override
    public void execute(TaskContext context)
            throws Exception {

        PreparedSource<SplitT> preparedSource =
                plan.getPreparedSource();

        SourceReader<FluxRow, SplitT> reader =
                null;

        Throwable failure = null;

        try {
            reader =
                    preparedSource
                            .getSource()
                            .createReader(
                                    preparedSource.getTables(),
                                    plan.getBatchSize());

            if (reader == null) {
                throw new IllegalStateException(
                        "Source returned a null reader");
            }

            if (plan.isDynamicSplitAssignment()) {
                executeDynamically(reader, preparedSource, context);
            } else {
                executeStatically(reader, preparedSource, context);
            }

        } catch (Throwable throwable) {
            failure = throwable;

            if (plan.getSplitQueue() != null) {
                plan.getSplitQueue().cancel(throwable);
            }
            context.getCancellationToken().cancel(throwable);
            outputGate.fail(throwable);

            throw propagate(throwable);

        } finally {
            Throwable closeFailure = null;

            if (reader != null) {
                try {
                    reader.close();
                } catch (Throwable throwable) {
                    closeFailure = throwable;
                }
            }

            try {
                outputGate.close();
            } catch (Throwable throwable) {
                if (closeFailure == null) {
                    closeFailure = throwable;
                } else {
                    closeFailure.addSuppressed(throwable);
                }
            }

            if (failure != null && closeFailure != null) failure.addSuppressed(closeFailure);
            else if (failure == null && closeFailure != null) throw propagate(closeFailure);
        }
    }

    private void executeStatically(SourceReader<FluxRow, SplitT> reader,
            PreparedSource<SplitT> preparedSource, TaskContext context) throws Exception {
        reader.open(plan.getSplits());
        while (!context.getCancellationToken().isCancelled()) {

                RecordBatch<FluxRow> batch =
                        reader.readBatch();

                if (batch == null) {
                    throw new IllegalStateException(
                            "SourceReader returned a null RecordBatch");
                }

                /*
                 * 兼容现有 SourceReader API。
                 * endOfInput 不再发送到 Channel。
                 */
                if (batch.isEndOfInput()) {
                for (SplitT split : plan.getSplits()) context.getMetrics().markSplitCompleted(split.splitId());
                break;
                }

                context.getMetrics().setCurrentPosition(batch.getDataSetId(), batch.getSplitId());

                RecordEnvelope<FluxRow> envelope =
                        createEnvelope(
                                batch,
                                preparedSource.getTables());

                context.getMetrics()
                        .incrementBatchCount();

                context.getMetrics()
                        .addSourceReadRecords(
                                batch.getRecords().size());

                outputGate.write(envelope);
        }
    }

    private void executeDynamically(SourceReader<FluxRow, SplitT> reader,
            PreparedSource<SplitT> preparedSource, TaskContext context) throws Exception {
        LocalSplitQueue<SplitT> queue = plan.getSplitQueue();
        reader.open();
        while (true) {
            SplitT split = queue.acquire(context.getCancellationToken());
            if (split == null) return;
            boolean splitOpen = false;
            try {
                reader.openSplit(split);
                splitOpen = true;
                while (true) {
                    RecordBatch<FluxRow> batch = reader.readBatch();
                    if (batch == null) throw new IllegalStateException("SourceReader returned a null RecordBatch");
                    if (batch.isEndOfInput()) break;
                    writeBatch(batch, preparedSource, context);
                }
                try {
                    reader.closeSplit();
                } finally {
                    splitOpen = false;
                }
                queue.complete(split);
                context.getMetrics().markSplitCompleted(split.splitId());
            } catch (Throwable failure) {
                queue.fail(split, failure);
                throw failure;
            } finally {
                if (splitOpen) reader.closeSplit();
            }
        }
    }

    private void writeBatch(RecordBatch<FluxRow> batch, PreparedSource<SplitT> preparedSource,
            TaskContext context) throws Exception {
        context.getMetrics().setCurrentPosition(batch.getDataSetId(), batch.getSplitId());
        RecordEnvelope<FluxRow> envelope = createEnvelope(batch, preparedSource.getTables());
        context.getMetrics().incrementBatchCount();
        context.getMetrics().addSourceReadRecords(batch.getRecords().size());
        outputGate.write(envelope);
    }

    private RecordEnvelope<FluxRow> createEnvelope(
            RecordBatch<FluxRow> batch,
            Map<TablePath, CatalogTable> tables) {

        String dataSetId =
                batch.getDataSetId();

        if (dataSetId == null
                || dataSetId.trim().isEmpty()) {

            throw new IllegalStateException(
                    "RecordBatch dataSetId must not be blank");
        }

        TablePath tablePath =
                TablePath.parse(dataSetId);

        CatalogTable catalogTable =
                tables.get(tablePath);

        if (catalogTable == null) {
            throw new IllegalStateException(
                    "No discovered source table for batch: "
                            + dataSetId);
        }

        return new RecordEnvelope<FluxRow>(
                tablePath,
                catalogTable,
                batch);
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
