package com.baize.flux.connector.jdbc.sink;

import com.baize.flux.api.sink.SinkWriter;
import com.baize.flux.api.source.RecordBatch;
import com.baize.flux.api.table.catalog.CatalogTable;
import com.baize.flux.api.table.type.FluxRow;
import com.baize.flux.connector.jdbc.config.JdbcSinkConfig;

/** Local JDBC sink writer backed by {@link JdbcOutputFormat}. */
public final class JdbcSinkWriter implements SinkWriter<FluxRow> {
    private final JdbcOutputFormat outputFormat;
    public JdbcSinkWriter(JdbcSinkConfig config) { outputFormat = new JdbcOutputFormatBuilder().withConfig(config).build(); }
    @Override public void write(RecordBatch<FluxRow> batch, CatalogTable sourceTable) throws Exception { if (batch == null || batch.isEndOfInput()) return; outputFormat.write(batch.getRecords(), sourceTable); }
    @Override public void close() throws Exception { outputFormat.close(); }
}
