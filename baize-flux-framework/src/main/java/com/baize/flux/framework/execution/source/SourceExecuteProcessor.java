package com.baize.flux.framework.execution.source;

import com.baize.flux.api.source.Source;
import com.baize.flux.api.source.SourceReader;
import com.baize.flux.api.source.SourceSplit;
import com.baize.flux.api.table.type.FluxRow;
import com.baize.flux.framework.factory.PreparedSource;

import java.util.List;
import java.util.Objects;

/**
 * 离线 Source 执行处理器。
 *
 * 当前采用单 Reader 顺序读取全部分片。
 * 后续可扩展为多个 SourceTask 并行执行。
 */
public final class SourceExecuteProcessor {

    public <SplitT extends SourceSplit> void execute(
            SourceAction<SplitT> action,
            RecordBatchConsumer<FluxRow> consumer)
            throws Exception {

        Objects.requireNonNull(
                action,
                "action must not be null");

        Objects.requireNonNull(
                consumer,
                "consumer must not be null");

        PreparedSource<SplitT> preparedSource =
                action.getPreparedSource();

        Source<SplitT> source =
                preparedSource.getSource();

        List<SplitT> splits =
                source.createSplits(
                        preparedSource.getTables());

        if (splits == null) {
            throw new IllegalStateException(
                    "Source returned null splits");
        }

        if (splits.isEmpty()) {
            return;
        }

        SourceReader<FluxRow, SplitT> reader =
                source.createReader(
                        preparedSource.getTables(),
                        action.getBatchSize());

        SourceTask<FluxRow, SplitT> task =
                new SourceTask<>(
                        reader,
                        splits,
                        consumer);

        task.execute();
    }
}