package com.baize.flux.runner;
import com.baize.flux.config.*; import com.baize.flux.plugin.*; import com.baize.flux.planner.*; import com.baize.flux.engine.*; import java.nio.file.*;
/** Standalone bounded-job process. Connector jars are supplied on its classpath. */
public final class RunnerMain { public static void main(String[] args) throws Exception {if(args.length!=1){System.err.println("Usage: flux-runner <job.properties>");System.exit(2);} var config=new PropertiesJobConfigParser().parse(Path.of(args[0])); var plan=new DefaultPlanner().plan(config,new FactoryRegistry(Thread.currentThread().getContextClassLoader()));var result=new BatchPipelineExecutor().execute(plan);System.out.printf("job=%s rows-written=%d%n",config.jobName(),result.rowsWritten());}}
