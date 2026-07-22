package com.baize.flux.api.table;
import java.util.*;
public record RowBatch(TableSchema schema, List<FluxRow> rows) { public RowBatch { Objects.requireNonNull(schema); rows=List.copyOf(rows); } public int size(){return rows.size();} }
