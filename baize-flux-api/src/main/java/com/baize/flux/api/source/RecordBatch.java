package com.baize.flux.api.source;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Source 输出的数据批次。
 *
 * @param <T> 数据类型，例如 FluxRow、String、byte[] 等
 */
public final class RecordBatch<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final RecordBatch<?> END_OF_INPUT =
            new RecordBatch<>(
                    null,
                    null,
                    Collections.emptyList(),
                    true);

    /**
     * 当前批次所属的数据集。
     * <p>
     * JDBC Source 中通常对应表名。
     */
    private final String dataSetId;

    /**
     * 当前批次所属的 Source 分片。
     */
    private final String splitId;

    /**
     * 当前批次的数据。
     */
    private final List<T> records;

    /**
     * 是否已经读取完全部数据。
     */
    private final boolean endOfInput;

    private final long estimatedBytes;

    private RecordBatch(
            String dataSetId,
            String splitId,
            List<T> records,
            boolean endOfInput) {

        this.dataSetId = dataSetId;
        this.splitId = splitId;
        this.records = records;
        this.endOfInput = endOfInput;
        this.estimatedBytes = estimateRecords(records);
    }

    /**
     * 创建普通数据批次。
     */
    public static <T> RecordBatch<T> of(
            SourceSplit split,
            List<T> records) {

        Objects.requireNonNull(split, "split must not be null");
        Objects.requireNonNull(records, "records must not be null");

        if (records.isEmpty()) {
            throw new IllegalArgumentException(
                    "records must not be empty");
        }

        return new RecordBatch<>(
                split.dataSetId(),
                split.splitId(),
                Collections.unmodifiableList(
                        new ArrayList<>(records)),
                false);
    }

    /**
     * 创建 Source 读取结束标记。
     */
    @SuppressWarnings("unchecked")
    public static <T> RecordBatch<T> endOfInput() {
        return (RecordBatch<T>) END_OF_INPUT;
    }

    public String getDataSetId() {
        return dataSetId;
    }

    public String getSplitId() {
        return splitId;
    }

    public List<T> getRecords() {
        return records;
    }

    /** Approximate bytes occupied by this batch's records. */
    public long getEstimatedBytes() {
        return estimatedBytes;
    }

    private static long estimateRecords(List<?> records) {
        long total = 16L;
        for (Object record : records) {
            long size = RecordSizeEstimator.estimate(record);
            total = total > Long.MAX_VALUE - size ? Long.MAX_VALUE : total + size;
        }
        return total;
    }

    public int size() {
        return records.size();
    }

    public boolean isEndOfInput() {
        return endOfInput;
    }
}