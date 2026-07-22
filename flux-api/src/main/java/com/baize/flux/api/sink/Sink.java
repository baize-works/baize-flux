package com.baize.flux.api.sink; import com.baize.flux.api.table.TableSchema; public interface Sink { void validateInputSchema(TableSchema schema); SinkWriter createWriter(); }
