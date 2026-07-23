package com.baize.flux.api.table;

/**
 * Immutable, connector-neutral row represented in source column order.
 */
public final class FluxRow {
    private final RowType rowType;
    private final java.util.List<Object> values;

    public FluxRow(RowType rowType, java.util.List<Object> values) {
        this.rowType = java.util.Objects.requireNonNull(rowType, "rowType");
        if (values == null || values.size() != rowType.fieldCount())
            throw new IllegalArgumentException("values must match row type");
        this.values = java.util.Collections.unmodifiableList(new java.util.ArrayList<>(values));
    }

    public RowType rowType() {
        return rowType;
    }

    public Object getField(int index) {
        return values.get(index);
    }

    public java.util.List<Object> values() {
        return values;
    }
}
