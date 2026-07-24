package com.baize.flux.connector.jdbc.sink;

import com.baize.flux.api.sink.SinkWriter;
import com.baize.flux.api.sink.CommitScope;
import com.baize.flux.api.sink.PreparedSinkMetadata;
import com.baize.flux.api.source.RecordBatch;
import com.baize.flux.api.table.catalog.CatalogTable;
import com.baize.flux.api.table.type.FluxRow;
import com.baize.flux.connector.jdbc.config.JdbcSinkConfig;

import java.util.List;
import java.util.Objects;

/**
 * JDBC SinkWriter。
 *
 * <p>一个 SinkTask 使用一个 JdbcSinkWriter，
 * 对应一条独立 JDBC 事务。
 */
public final class JdbcSinkWriter
        implements SinkWriter<FluxRow> {

    private final JdbcOutputFormat outputFormat;

    private final JdbcSinkConfig config;

    public JdbcSinkWriter(
            JdbcSinkConfig config,
            PreparedSinkMetadata metadata) {

        this.config = Objects.requireNonNull(config, "config must not be null");
        this.outputFormat =
                new JdbcOutputFormatBuilder()
                        .withMetadata(Objects.requireNonNull(metadata, "metadata must not be null"))
                        .withConfig(
                                this.config)
                        .build();
    }

    @Override
    public void open() {
        outputFormat.open();
    }

    @Override
    public void write(
            RecordBatch<FluxRow> batch,
            CatalogTable sourceTable)
            throws Exception {

        if (batch == null
                || batch.isEndOfInput()
                || batch.getRecords().isEmpty()) {
            return;
        }

        outputFormat.write(
                batch.getRecords(),
                sourceTable);
    }

    @Override
    public void commit() throws Exception {
        outputFormat.commit();
    }

    @Override
    public void abort() throws Exception {
        outputFormat.rollback();
    }

    @Override
    public CommitScope getCommitScope() {
        return CommitScope.TASK_LOCAL;
    }

    @Override
    public String getRetryAdvice() {
        if (config.getWriteMode() == com.baize.flux.connector.jdbc.config.JdbcWriteMode.UPSERT) {
            return "JDBC UPSERT is usually safe to rerun when primary keys are stable, but safety depends on dialect semantics.";
        }
        switch (config.getDataSaveMode()) {
            case APPEND_DATA:
                return "JDBC APPEND may duplicate rows on retry; verify committed targets before rerunning.";
            case DROP_DATA:
                return "JDBC DROP_DATA may have cleared target data before failure; inspect and restore/reload as needed before rerunning.";
            case CUSTOM_PROCESSING:
                return "JDBC CUSTOM_PROCESSING retry safety is determined by the configured SQL; review its effects before rerunning.";
            default:
                return "JDBC retry safety depends on the configured save and write modes; verify committed targets before rerunning.";
        }
    }

    @Override
    public void close() throws Exception {
        outputFormat.close();
    }

    /**
     * Returns immutable snapshots of rows skipped under {@code dirty_data_policy=SKIP}.
     */
    public List<JdbcRowError> getRowErrors() {
        return outputFormat.getRowErrors();
    }
}
