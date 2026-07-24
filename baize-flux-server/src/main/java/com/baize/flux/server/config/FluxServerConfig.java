package com.baize.flux.server.config;
/** Server 启动参数的不可变配置。 */
public final class FluxServerConfig { private final String host; private final int port,jobThreads; public FluxServerConfig(String host,int port,int jobThreads){if(port<1||port>65535)throw new IllegalArgumentException("port must be between 1 and 65535");this.host=host;this.port=port;this.jobThreads=Math.max(1,jobThreads);} public String getHost(){return host;} public int getPort(){return port;} public int getJobThreads(){return jobThreads;} }
