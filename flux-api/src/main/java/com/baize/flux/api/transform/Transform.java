package com.baize.flux.api.transform; import com.baize.flux.api.table.*; public interface Transform { TableSchema transformSchema(TableSchema input); RowBatch transform(RowBatch input); }
