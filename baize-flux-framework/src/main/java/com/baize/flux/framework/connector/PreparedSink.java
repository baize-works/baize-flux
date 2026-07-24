package com.baize.flux.framework.connector;

import com.baize.flux.api.configuration.ReadonlyConfig;
import com.baize.flux.api.sink.Sink;
import com.baize.flux.api.table.catalog.CatalogTable;
import com.baize.flux.api.table.catalog.TablePath;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** 已完成 Job 级准备的不可变 Sink 及目标表元数据。 */
public final class PreparedSink {
    private final String factoryIdentifier;
    private final ReadonlyConfig options;
    private final Sink sink;
    private final Map<TablePath, CatalogTable> preparedTargetTables;
    public PreparedSink(String factoryIdentifier, ReadonlyConfig options, Sink sink,
            Map<TablePath, CatalogTable> preparedTargetTables) {
        this.factoryIdentifier = Objects.requireNonNull(factoryIdentifier, "factoryIdentifier must not be null");
        this.options = Objects.requireNonNull(options, "options must not be null");
        this.sink = Objects.requireNonNull(sink, "sink must not be null");
        this.preparedTargetTables = Collections.unmodifiableMap(new LinkedHashMap<TablePath, CatalogTable>(Objects.requireNonNull(preparedTargetTables, "preparedTargetTables must not be null")));
    }
    public String getFactoryIdentifier() { return factoryIdentifier; }
    public ReadonlyConfig getOptions() { return options; }
    /** 不可变 Sink；Writer 必须由 SinkTask 创建。 */
    public Sink getSink() { return sink; }
    public Map<TablePath, CatalogTable> getPreparedTargetTables() { return preparedTargetTables; }
}
