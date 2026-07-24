# Baize Flux

Baize Flux is a local, batch-oriented data synchronization engine. It currently provides JDBC source and sink connectors, HOCON job parsing, parallel source/sink execution, and execution metrics.

## Quick start

Build the complete project and its deployable archives with JDK 8+ and Maven 3.8.1+:

```bash
mvn --batch-mode clean verify
```

Run directly from source:

```bash
mvn -pl baize-flux-launcher -am compile exec:java \
  -Dexec.mainClass=com.baize.flux.launcher.LocalSyncLauncher \
  -Dexec.args=baize-flux-launcher/examples/jdbc-single-table.conf
```

Or extract `baize-flux-launcher/target/baize-flux-1.0.0.tar.gz`, copy and edit `config/baize-flux.yaml`, then run:

```bash
bin/baize-flux.sh --config config/baize-flux.yaml
```

The configuration file has a `.yaml` deployment-friendly name but uses HOCON syntax. Do not commit real JDBC credentials. See the [deployment and operations guide](docs/deployment.md) for packaging, CI, Docker, JVM options, Log4j2, security, rollback, and production guidance.
