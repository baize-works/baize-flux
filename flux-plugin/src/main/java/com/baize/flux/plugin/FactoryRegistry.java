package com.baize.flux.plugin;
import com.baize.flux.api.factory.*; import java.util.*;
/** Discovers connector factories through JDK ServiceLoader; loading stays outside the engine. */
public final class FactoryRegistry { private final Map<String,SourceFactory> sources; private final Map<String,SinkFactory> sinks;
 public FactoryRegistry(ClassLoader loader){sources=index(ServiceLoader.load(SourceFactory.class,loader)); sinks=index(ServiceLoader.load(SinkFactory.class,loader));}
 private static <T extends Factory> Map<String,T> index(ServiceLoader<T> factories){var r=new HashMap<String,T>(); factories.forEach(f->{if(r.put(f.identifier(),f)!=null)throw new IllegalStateException("Duplicate factory: "+f.identifier());});return Map.copyOf(r);}
 public SourceFactory source(String id){return required(sources,id,"source");} public SinkFactory sink(String id){return required(sinks,id,"sink");} private static <T> T required(Map<String,T> fs,String id,String type){var f=fs.get(id);if(f==null)throw new IllegalArgumentException("No "+type+" factory registered for: "+id);return f;}
}
