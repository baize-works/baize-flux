package com.baize.flux.api.table;

/** Describes the ordered columns carried by a {@link FluxRow}. */
public final class RowType {
    private final java.util.List<String> fieldNames;
    public RowType(java.util.List<String> fieldNames) {
        if (fieldNames == null || fieldNames.isEmpty()) throw new IllegalArgumentException("fieldNames must not be empty");
        this.fieldNames = java.util.Collections.unmodifiableList(new java.util.ArrayList<>(fieldNames));
    }
    public java.util.List<String> fieldNames() { return fieldNames; }
    public int fieldCount() { return fieldNames.size(); }
}
