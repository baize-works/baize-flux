package com.baize.flux.framework.connector;

import com.baize.flux.api.connector.ConnectorDescriptor;
import com.baize.flux.api.factory.Factory;
import com.baize.flux.api.factory.SinkFactory;
import com.baize.flux.api.table.factory.TableSourceFactory;
import com.baize.flux.framework.plugin.ConnectorClassLoader;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/** Discovers application-classpath and isolated connector-directory factories. */
public final class FactoryRegistry implements AutoCloseable {
    private final Map<String, TableSourceFactory<?>> sourceFactories;
    private final Map<String, SinkFactory> sinkFactories;
    private final List<ConnectorClassLoader> pluginLoaders;

    public FactoryRegistry(ClassLoader classLoader) { this(classLoader, Collections.<Path>emptyList()); }
    public FactoryRegistry(ClassLoader classLoader, Path... pluginDirectories) { this(classLoader, paths(pluginDirectories)); }
    public FactoryRegistry(ClassLoader classLoader, Iterable<Path> pluginDirectories) {
        Objects.requireNonNull(classLoader, "classLoader must not be null");
        Map<String, TableSourceFactory<?>> sources = new LinkedHashMap<String, TableSourceFactory<?>>();
        Map<String, SinkFactory> sinks = new LinkedHashMap<String, SinkFactory>();
        pluginLoaders = new ArrayList<ConnectorClassLoader>();
        discover(classLoader, null, false, sources, sinks);
        for (Path directory : pluginDirectories) {
            String path = directory.toAbsolutePath().toString();
            try {
                ConnectorClassLoader loader = new ConnectorClassLoader(pluginUrls(directory), classLoader);
                pluginLoaders.add(loader);
                discover(loader, path, true, sources, sinks);
            } catch (ConnectorException e) { close(); throw e; }
            catch (Throwable e) { close(); throw new ConnectorException("Could not load connector plugin at " + path, e); }
        }
        sourceFactories = Collections.unmodifiableMap(sources); sinkFactories = Collections.unmodifiableMap(sinks);
    }
    public static FactoryRegistry discover(ClassLoader loader) { return new FactoryRegistry(loader == null ? Thread.currentThread().getContextClassLoader() : loader); }
    public static FactoryRegistry discover(ClassLoader loader, Path... directories) { return new FactoryRegistry(loader == null ? Thread.currentThread().getContextClassLoader() : loader, directories); }
    public TableSourceFactory<?> getSourceFactory(String identifier) { TableSourceFactory<?> f=sourceFactories.get(normalize(identifier)); if(f==null) throw new ConnectorException("Could not find source factory for identifier '"+identifier+"'. Available identifiers: "+sourceFactories.keySet()); return f; }
    public SinkFactory getSinkFactory(String identifier) { SinkFactory f=sinkFactories.get(normalize(identifier)); if(f==null) throw new ConnectorException("Could not find sink factory for identifier '"+identifier+"'. Available identifiers: "+sinkFactories.keySet()); return f; }
    private void discover(ClassLoader loader, String path, boolean pluginOnly, Map<String, TableSourceFactory<?>> sources, Map<String, SinkFactory> sinks) {
        try {
            for (TableSourceFactory<?> f : ServiceLoader.load(TableSourceFactory.class, loader)) if (!pluginOnly || f.getClass().getClassLoader() == loader) put(sources, f, "source", path);
            for (SinkFactory f : ServiceLoader.load(SinkFactory.class, loader)) if (!pluginOnly || f.getClass().getClassLoader() == loader) put(sinks, f, "sink", path);
        } catch (ServiceConfigurationError e) { throw new ConnectorException("Could not load connector factories" + (path == null ? "" : " from plugin " + path), e); }
    }
    private static <T extends Factory> void put(Map<String,T> target, T factory, String kind, String path) {
        String key=normalize(factory.factoryIdentifier()); T previous=target.put(key, factory);
        if(previous != null) { target.put(key, previous); throw new ConnectorException("Duplicated "+kind+" factory identifier '"+factory.factoryIdentifier()+"': "+describe(previous)+" conflicts with "+describe(factory, path)); }
    }
    private static String describe(Factory f) { return describe(f, f.connectorDescriptor().getPluginPath()); }
    private static String describe(Factory f, String discoveredPath) { ConnectorDescriptor d=f.connectorDescriptor(); String path=discoveredPath == null ? d.getPluginPath() : discoveredPath; return f.getClass().getName()+" (version "+d.getVersion()+", plugin "+(path == null ? "classpath" : path)+")"; }
    private static URL[] pluginUrls(Path directory) throws IOException { if (!Files.isDirectory(directory)) throw new IOException("Plugin directory does not exist or is not a directory: " + directory); List<URL> urls=new ArrayList<URL>(); urls.add(directory.toUri().toURL()); try (java.util.stream.Stream<Path> stream=Files.list(directory)) { for (Path p : (Iterable<Path>)stream.filter(p -> p.getFileName().toString().endsWith(".jar"))::iterator) urls.add(p.toUri().toURL()); } return urls.toArray(new URL[urls.size()]); }
    private static List<Path> paths(Path... dirs) { List<Path> result=new ArrayList<Path>(); if(dirs != null) Collections.addAll(result, dirs); return result; }
    private static String normalize(String id) { Objects.requireNonNull(id,"factory identifier must not be null"); String n=id.trim().toLowerCase(Locale.ROOT); if(n.isEmpty()) throw new IllegalArgumentException("factory identifier must not be blank"); return n; }
    @Override public void close() { for (ConnectorClassLoader loader : pluginLoaders) try { loader.close(); } catch (IOException ignored) { } pluginLoaders.clear(); }
}
