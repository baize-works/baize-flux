package com.baize.flux.api.sink;

import com.baize.flux.api.table.catalog.CatalogTable;
import com.baize.flux.api.table.catalog.TablePath;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Sink Writer 的 Task 级运行上下文。 */
public final class SinkWriterContext {
    private final TaskId taskId;
    private final int subtaskIndex;
    private final int parallelism;
    private final ClassLoader classLoader;
    private final SinkWriterMetrics metrics;
    private final Map<TablePath, CatalogTable> preparedTargetTables;
    public SinkWriterContext(TaskId taskId, int subtaskIndex, int parallelism, ClassLoader classLoader,
            SinkWriterMetrics metrics, Map<TablePath, CatalogTable> preparedTargetTables) {
        this.taskId = Objects.requireNonNull(taskId, "taskId must not be null");
        if (subtaskIndex < 0 || subtaskIndex >= parallelism || parallelism <= 0) throw new IllegalArgumentException("invalid subtaskIndex or parallelism");
        this.subtaskIndex = subtaskIndex; this.parallelism = parallelism;
        this.classLoader = Objects.requireNonNull(classLoader, "classLoader must not be null");
        this.metrics = Objects.requireNonNull(metrics, "metrics must not be null");
        this.preparedTargetTables = Collections.unmodifiableMap(new LinkedHashMap<TablePath, CatalogTable>(Objects.requireNonNull(preparedTargetTables, "preparedTargetTables must not be null")));
    }
    public TaskId getTaskId() { return taskId; }
    public int getSubtaskIndex() { return subtaskIndex; }
    public int getParallelism() { return parallelism; }
    public ClassLoader getClassLoader() { return classLoader; }
    public SinkWriterMetrics getMetrics() { return metrics; }
    public Map<TablePath, CatalogTable> getPreparedTargetTables() { return preparedTargetTables; }
}
