# JDBC connector

The `jdbc` source is bounded and can synchronize one or more tables in one
offline job. It does not poll for changes or implement CDC. Configure
`table_list` with table objects for a multi-table job; each emitted batch keeps
its source table identity so the JDBC sink can create and write the matching
target table.

```hocon
source {
  type = "jdbc"
  url = "jdbc:mysql://localhost:3306/flux_test"
  table_list = [
    { table_path = "flux_test.user_info" },
    { table_path = "flux_test.orders" }
  ]
}

sink {
  type = "jdbc"
  url = "jdbc:mysql://localhost:3306/flux_test"
  # `table` is an alias for `table_path`.
  table = "sink_${schema_name}_${table_name}"
}
```

The sink expands `${table_name}` with the source table name. `${schema_name}`
uses the source schema; for MySQL's `database.table` paths it uses the database
name. A fixed `table` value, such as `public.sink_table`, writes all input to
that target. If `table`/`table_path` is configured, Flux generates the
INSERT/UPSERT SQL for the resolved target and ignores `custom_sql`/`query`.

## Options

| Option | Required | Default | Description |
| --- | --- | --- | --- |
| `url` | yes | — | JDBC connection URL. |
| `table_path` | one of `table_path` or `table_list` | — | Source table path. |
| `table_list` | one of `table_path` or `table_list` | — | List of `{ table_path = "..." }` source-table objects. |
| `query` | no | — | SQL query to read for a single table; requires `table_path`. |
| `username` | no | empty | JDBC user name. |
| `password` | no | empty | JDBC password. |
| `driver` | no | — | JDBC driver class to load before connecting. |
| `fetch-size` | no | `1000` | Maximum number of rows emitted in one batch. |

The result columns retain their query order and use JDBC column labels as the
`FluxRow` field names.
