# Baize Flux

Baize Flux is a bounded, local-process data transfer framework. It deliberately keeps a small API: a source plans finite splits, a runner reads one split at a time in batches, optional transforms run, and a sink writes and commits the job. There are no streaming checkpoints, distributed workers, or engine translations.

## Initial modules

* `flux-config` — immutable job configuration and `.properties` parser.
* `flux-api` — table, bounded source, sink, transform, and factory contracts.
* `flux-plugin` — JDK `ServiceLoader` factory discovery, outside the engine.
* `flux-planner` / `flux-engine` — physical pipeline plan and sequential executor.
* `flux-runner` / `flux-launcher` — independent job JVM entry point and process launcher.
* `flux-connectors/flux-connector-jdbc` — one JDBC connector providing both source and sink factories.
* `flux-e2e` — MySQL-compatible JDBC copy test using H2's MySQL mode.

## MySQL to MySQL

Copy and complete [`examples/mysql-to-mysql.properties`](examples/mysql-to-mysql.properties), then build and run with the runner plus JDBC connector on the classpath:

```bash
mvn verify
java -cp 'flux-runner/target/flux-runner-0.1.0-SNAPSHOT.jar:flux-connectors/flux-connector-jdbc/target/flux-connector-jdbc-0.1.0-SNAPSHOT.jar:...' com.baize.flux.runner.RunnerMain examples/mysql-to-mysql.properties
```

The JDBC source requires `url` and `query`; the JDBC sink requires `url`, `table`, and ordered `columns`. Source column labels must align with sink columns. The implementation uses one finite JDBC split for this first flow and commits the destination transaction after all batches complete.
