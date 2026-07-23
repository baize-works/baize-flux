package com.baize.flux.framework.execution.sink;

import com.baize.flux.api.sink.SinkWriter;
import com.baize.flux.api.source.RecordBatch;
import com.baize.flux.api.table.catalog.CatalogTable;
import com.baize.flux.api.table.catalog.TablePath;
import com.baize.flux.api.table.type.FluxRow;

import java.util.Map;
import java.util.Objects;

/**
 * Routes source batches to a sink with the matching discovered table schema.
 */
public final class SinkExecuteProcessor {
    public void execute(RecordBatch<FluxRow> batch, Map<TablePath, CatalogTable> tables, SinkWriter<FluxRow> writer) throws Exception {
        Objects.requireNonNull(batch, "batch must not be null");
        Objects.requireNonNull(tables, "tables must not be null");
        Objects.requireNonNull(writer, "writer must not be null");
        if (batch.isEndOfInput()) return;
        CatalogTable table = tables.get(TablePath.parse(batch.getDataSetId()));
        if (table == null)
            throw new IllegalStateException("No discovered source table for batch: " + batch.getDataSetId());
        writer.write(batch, table);
    }
}
