package com.baize.flux.connector.jdbc.sink;

import com.baize.flux.api.sink.SinkWriter;
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

    public JdbcSinkWriter(
            JdbcSinkConfig config) {

        this.outputFormat =
                new JdbcOutputFormatBuilder()
                        .withConfig(
                                Objects.requireNonNull(
                                        config,
                                        "config must not be null"))
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
    public void rollback() throws Exception {
        outputFormat.rollback();
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
