package com.baize.flux.config;
import java.io.*; import java.nio.file.*; import java.util.*;
/** Parses a deliberately small, dependency-free .properties job format. */
public final class PropertiesJobConfigParser {
 public JobConfig parse(Path path) throws IOException { var p=new Properties(); try(var in=Files.newInputStream(path)){p.load(in);} return new JobConfig(req(p,"job.name"), connector(p,"source."), List.of(), connector(p,"sink."), Integer.parseInt(p.getProperty("runtime.batch-size","1000"))); }
 private static JobConfig.ConnectorConfig connector(Properties p,String prefix){var options=new HashMap<String,String>(); for(var name:p.stringPropertyNames()) if(name.startsWith(prefix) && !name.equals(prefix+"type")) options.put(name.substring(prefix.length()),p.getProperty(name)); return new JobConfig.ConnectorConfig(req(p,prefix+"type"),options);}
 private static String req(Properties p,String key){var v=p.getProperty(key); if(v==null||v.isBlank()) throw new IllegalArgumentException("Missing required property: "+key); return v;}
}
