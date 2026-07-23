package com.baize.flux.api.sink;

import com.baize.flux.api.source.RecordBatch;
import com.baize.flux.api.table.catalog.CatalogTable;

/**
 * Transactional destination writer for one local job.
 */
public interface SinkWriter<T> extends AutoCloseable {

    void write(RecordBatch<T> batch, CatalogTable sourceTable) throws Exception;

    @Override
    void close() throws Exception;
}
