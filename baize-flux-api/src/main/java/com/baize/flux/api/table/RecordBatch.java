package com.baize.flux.api.table;

/** A non-null batch of records, or the explicit end-of-input marker. */
public final class RecordBatch<T> {
    private static final RecordBatch<?> END = new RecordBatch<Object>(java.util.Collections.emptyList(), true);
    private final java.util.List<T> records;
    private final boolean endOfInput;
    private RecordBatch(java.util.List<T> records, boolean endOfInput) { this.records = java.util.Collections.unmodifiableList(new java.util.ArrayList<>(records)); this.endOfInput = endOfInput; }
    public static <T> RecordBatch<T> of(java.util.List<T> records) { return new RecordBatch<T>(java.util.Objects.requireNonNull(records, "records"), false); }
    @SuppressWarnings("unchecked") public static <T> RecordBatch<T> endOfInput() { return (RecordBatch<T>) END; }
    public java.util.List<T> records() { return records; }
    public boolean isEmpty() { return records.isEmpty(); }
    public boolean isEndOfInput() { return endOfInput; }
}
