package com.baize.flux.api.channel;

import com.baize.flux.api.table.type.FluxRow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * FluxRow 数据批次。
 *
 * 离线同步按批传输，减少线程切换和队列操作次数。
 */
public final class FluxRowBatch implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final FluxRowBatch END_OF_INPUT =
            new FluxRowBatch(
                    Collections.<FluxRow>emptyList(),
                    true);

    private final List<FluxRow> rows;
    private final boolean endOfInput;

    private FluxRowBatch(
            List<FluxRow> rows,
            boolean endOfInput) {

        this.rows = rows;
        this.endOfInput = endOfInput;
    }

    public static FluxRowBatch of(List<FluxRow> rows) {
        Objects.requireNonNull(rows, "rows must not be null");

        if (rows.isEmpty()) {
            throw new IllegalArgumentException(
                    "rows must not be empty");
        }

        List<FluxRow> copiedRows =
                Collections.unmodifiableList(
                        new ArrayList<>(rows));

        return new FluxRowBatch(copiedRows, false);
    }

    /**
     * 创建数据读取结束标记。
     *
     * 离线模式只需要结束标记，不需要 Checkpoint Barrier。
     */
    public static FluxRowBatch endOfInput() {
        return END_OF_INPUT;
    }

    public List<FluxRow> getRows() {
        return rows;
    }

    public int size() {
        return rows.size();
    }

    public boolean isEndOfInput() {
        return endOfInput;
    }
}