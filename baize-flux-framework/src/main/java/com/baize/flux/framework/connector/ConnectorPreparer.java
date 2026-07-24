package com.baize.flux.framework.connector;

import com.baize.flux.api.configuration.util.ConfigValidator;
import com.baize.flux.api.factory.SinkFactory;
import com.baize.flux.api.source.Source;
import com.baize.flux.api.source.SourceFactoryContext;
import com.baize.flux.api.source.SourceSplit;
import com.baize.flux.api.table.catalog.CatalogTable;
import com.baize.flux.api.table.catalog.TablePath;
import com.baize.flux.api.table.factory.TableSourceFactory;
import com.baize.flux.framework.job.JobDefinition;
import com.baize.flux.framework.job.SinkDefinition;
import com.baize.flux.framework.job.SourceDefinition;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Connector 准备器。
 *
 * <p>负责：
 *
 * <ul>
 *     <li>查找 Factory</li>
 *     <li>校验配置</li>
 *     <li>创建 Source</li>
 *     <li>发现 Source Schema</li>
 *     <li>准备 Sink Factory</li>
 * </ul>
 */
public final class ConnectorPreparer {

    private final FactoryRegistry registry;

    private final ClassLoader classLoader;

    public ConnectorPreparer(
            FactoryRegistry registry,
            ClassLoader classLoader) {

        this.registry =
                Objects.requireNonNull(
                        registry,
                        "registry must not be null");

        this.classLoader =
                Objects.requireNonNull(
                        classLoader,
                        "classLoader must not be null");
    }

    public PreparedJob prepare(
            JobDefinition definition) throws Exception {

        Objects.requireNonNull(
                definition,
                "definition must not be null");

        PreparedSource<?> source =
                prepareSource(
                        definition.getSource());

        List<PreparedSink> sinks =
                prepareSinks(
                        definition.getSink(),
                        definition.getExecutionConfig()
                                .getSinkParallelism());

        return new PreparedJob(
                definition.getName(),
                source,
                sinks,
                definition.getExecutionConfig());
    }

    private PreparedSource<?> prepareSource(
            SourceDefinition definition) throws Exception {

        TableSourceFactory<?> factory =
                registry.getSourceFactory(
                        definition.getType());

        ConfigValidator.of(
                definition.getOptions())
                .validate(
                        factory.optionRule());

        SourceFactoryContext context =
                new SourceFactoryContext(
                        definition.getOptions(),
                        classLoader);

        return createPreparedSource(
                definition.getType(),
                factory,
                context);
    }

    private <SplitT extends SourceSplit>
    PreparedSource<SplitT> createPreparedSource(
            String identifier,
            TableSourceFactory<SplitT> factory,
            SourceFactoryContext context) throws Exception {

        Source<SplitT> source =
                factory.createSource(context);

        if (source == null) {
            throw new ConnectorException(
                    "Source factory '"
                            + identifier
                            + "' returned a null source");
        }

        List<CatalogTable> catalogTables =
                factory.discoverTableSchemas(context);

        Map<TablePath, CatalogTable> tableMap =
                buildTableMap(
                        identifier,
                        catalogTables);

        return new PreparedSource<SplitT>(
                identifier,
                source,
                tableMap);
    }

    private List<PreparedSink> prepareSinks(
            SinkDefinition definition,
            int parallelism) {

        SinkFactory factory =
                registry.getSinkFactory(
                        definition.getType());

        ConfigValidator.of(
                definition.getOptions())
                .validate(
                        factory.optionRule());

        List<PreparedSink> sinks =
                new java.util.ArrayList<PreparedSink>(
                        parallelism);

        for (int i = 0; i < parallelism; i++) {
            sinks.add(
                    new PreparedSink(
                            definition.getType(),
                            factory,
                            definition.getOptions()));
        }

        return sinks;
    }

    private Map<TablePath, CatalogTable> buildTableMap(
            String factoryIdentifier,
            List<CatalogTable> catalogTables) {

        if (catalogTables == null) {
            throw new ConnectorException(
                    "Source factory '"
                            + factoryIdentifier
                            + "' returned null catalog tables");
        }

        if (catalogTables.isEmpty()) {
            throw new ConnectorException(
                    "No source tables were discovered by factory '"
                            + factoryIdentifier
                            + "'");
        }

        Map<TablePath, CatalogTable> result =
                new LinkedHashMap<
                        TablePath,
                        CatalogTable>();

        for (CatalogTable catalogTable : catalogTables) {
            if (catalogTable == null) {
                throw new ConnectorException(
                        "Source factory '"
                                + factoryIdentifier
                                + "' returned a null CatalogTable");
            }

            TablePath tablePath =
                    catalogTable.getTablePath();

            if (tablePath == null) {
                throw new ConnectorException(
                        "CatalogTable returned by source factory '"
                                + factoryIdentifier
                                + "' has no TablePath");
            }

            CatalogTable previous =
                    result.put(
                            tablePath,
                            catalogTable);

            if (previous != null) {
                throw new ConnectorException(
                        "Duplicated source table path: "
                                + tablePath);
            }
        }

        return result;
    }
}
