package com.baize.flux.api.sink;

import com.baize.flux.api.configuration.ReadonlyConfig;
import com.baize.flux.api.table.catalog.CatalogTable;
import com.baize.flux.api.table.catalog.TablePath;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Sink Factory 的 Job 级创建上下文。
 *
 * <p>其中表元数据是准备阶段的稳定快照，Factory 可据此生成可被所有 Writer 共享的不可变准备结果。
 */
public final class SinkFactoryContext {
    private final ReadonlyConfig options;
    private final ClassLoader classLoader;
    private final String jobName;
    private final Map<TablePath, CatalogTable> sourceTables;
    private final Map<TablePath, CatalogTable> preparedTargetTables;

    public SinkFactoryContext(ReadonlyConfig options, ClassLoader classLoader, String jobName,
            Map<TablePath, CatalogTable> sourceTables, Map<TablePath, CatalogTable> preparedTargetTables) {
        this.options = Objects.requireNonNull(options, "options must not be null");
        this.classLoader = Objects.requireNonNull(classLoader, "classLoader must not be null");
        this.jobName = Objects.requireNonNull(jobName, "jobName must not be null");
        this.sourceTables = immutableCopy(sourceTables, "sourceTables");
        this.preparedTargetTables = immutableCopy(preparedTargetTables, "preparedTargetTables");
    }
    private static Map<TablePath, CatalogTable> immutableCopy(Map<TablePath, CatalogTable> value, String name) {
        return Collections.unmodifiableMap(new LinkedHashMap<TablePath, CatalogTable>(Objects.requireNonNull(value, name + " must not be null")));
    }
    public ReadonlyConfig getOptions() { return options; }
    public ClassLoader getClassLoader() { return classLoader; }
    public String getJobName() { return jobName; }
    /** 已发现的源表。 */
    public Map<TablePath, CatalogTable> getSourceTables() { return sourceTables; }
    /** 已准备完成、供 Writer 使用的目标表元数据。 */
    public Map<TablePath, CatalogTable> getPreparedTargetTables() { return preparedTargetTables; }
}
