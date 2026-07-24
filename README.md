# Baize Flux

Baize Flux is a local, batch-oriented data synchronization engine. It currently provides JDBC source and sink connectors, HOCON job parsing, parallel source/sink execution, and execution metrics.

## Module architecture

The project is organized around dependency direction instead of technical catch-all modules:

```text
baize-flux-api          public connector, table, configuration, and error contracts
        ↑
baize-flux-framework    job planning, scheduling, channels, execution, and metrics
        ↑
baize-flux-connectors   connector implementations (for example, JDBC)
        ↑
baize-flux-launcher     executable assembly and local CLI entry point
```

`baize-flux-api` is the only extension-facing dependency. It owns the shared error contract
(`FluxRuntimeException`, `FluxErrorCode`, and API error codes), removing the former generic
`common` module. `baize-flux-framework` is the runtime module (the renamed and consolidated
core/engine responsibility); connectors depend on API contracts but not on framework internals.
The connector aggregator contains no inherited runtime dependencies—each connector declares the
libraries it needs explicitly.

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

Or extract `baize-flux-dist/target/baize-flux-1.0.0.tar.gz`, copy and edit `config/baize-flux.yaml`, then run:

```bash
bin/baize-flux.sh --config config/baize-flux.yaml
```

The configuration file has a `.yaml` deployment-friendly name but uses HOCON syntax. Do not commit real JDBC credentials. See the [deployment and operations guide](docs/deployment.md) for packaging, CI, Docker, JVM options, Log4j2, security, rollback, and production guidance.
