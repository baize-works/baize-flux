package com.baize.flux.framework.connector;

import com.baize.flux.api.factory.Factory;
import com.baize.flux.api.factory.SinkFactory;
import com.baize.flux.api.table.factory.TableSourceFactory;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * Connector Factory 注册中心。
 *
 * <p>统一负责 SourceFactory 和 SinkFactory 的 SPI 发现。
 */
public final class FactoryRegistry {

    private final Map<String, TableSourceFactory<?>>
            sourceFactories;

    private final Map<String, SinkFactory>
            sinkFactories;

    public FactoryRegistry(ClassLoader classLoader) {
        Objects.requireNonNull(
                classLoader,
                "classLoader must not be null");

        this.sourceFactories =
                discoverSourceFactories(classLoader);

        this.sinkFactories =
                discoverSinkFactories(classLoader);
    }

    public static FactoryRegistry discover(
            ClassLoader classLoader) {

        ClassLoader effectiveClassLoader =
                classLoader == null
                        ? Thread.currentThread()
                        .getContextClassLoader()
                        : classLoader;

        return new FactoryRegistry(
                effectiveClassLoader);
    }

    public TableSourceFactory<?> getSourceFactory(
            String identifier) {

        String normalized = normalize(identifier);

        TableSourceFactory<?> factory =
                sourceFactories.get(normalized);

        if (factory == null) {
            throw new ConnectorException(
                    "Could not find source factory for identifier '"
                            + identifier
                            + "'. Available identifiers: "
                            + sourceFactories.keySet());
        }

        return factory;
    }

    public SinkFactory getSinkFactory(
            String identifier) {

        String normalized = normalize(identifier);

        SinkFactory factory =
                sinkFactories.get(normalized);

        if (factory == null) {
            throw new ConnectorException(
                    "Could not find sink factory for identifier '"
                            + identifier
                            + "'. Available identifiers: "
                            + sinkFactories.keySet());
        }

        return factory;
    }

    private Map<String, TableSourceFactory<?>>
    discoverSourceFactories(
            ClassLoader classLoader) {

        Map<String, TableSourceFactory<?>> result =
                new LinkedHashMap<
                        String,
                        TableSourceFactory<?>>();

        try {
            ServiceLoader<TableSourceFactory> loader =
                    ServiceLoader.load(
                            TableSourceFactory.class,
                            classLoader);

            for (TableSourceFactory<?> factory : loader) {
                putFactory(
                        result,
                        factory.factoryIdentifier(),
                        factory,
                        "source");
            }

            return Collections.unmodifiableMap(result);

        } catch (ServiceConfigurationError error) {
            throw new ConnectorException(
                    "Could not load source factories",
                    error);
        }
    }

    private Map<String, SinkFactory>
    discoverSinkFactories(
            ClassLoader classLoader) {

        Map<String, SinkFactory> result =
                new LinkedHashMap<String, SinkFactory>();

        try {
            ServiceLoader<SinkFactory> loader =
                    ServiceLoader.load(
                            SinkFactory.class,
                            classLoader);

            for (SinkFactory factory : loader) {
                putFactory(
                        result,
                        factory.factoryIdentifier(),
                        factory,
                        "sink");
            }

            return Collections.unmodifiableMap(result);

        } catch (ServiceConfigurationError error) {
            throw new ConnectorException(
                    "Could not load sink factories",
                    error);
        }
    }

    private static <T extends Factory> void putFactory(
            Map<String, T> factories,
            String identifier,
            T factory,
            String kind) {

        String normalized = normalize(identifier);

        T previous =
                factories.put(
                        normalized,
                        factory);

        if (previous != null) {
            throw new ConnectorException(
                    "Duplicated "
                            + kind
                            + " factory identifier '"
                            + identifier
                            + "': "
                            + previous.getClass().getName()
                            + ", "
                            + factory.getClass().getName());
        }
    }

    private static String normalize(
            String identifier) {

        Objects.requireNonNull(
                identifier,
                "factory identifier must not be null");

        String normalized =
                identifier.trim()
                        .toLowerCase(Locale.ROOT);

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(
                    "factory identifier must not be blank");
        }

        return normalized;
    }
}