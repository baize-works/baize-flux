package com.baize.flux.api.table;
import java.util.*;
public record TableSchema(List<String> columns) { public TableSchema { columns=List.copyOf(columns); if(columns.isEmpty()) throw new IllegalArgumentException("schema requires columns"); } }
