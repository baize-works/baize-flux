# JDBC source connector

The `jdbc` source executes one SQL query and emits its result set once. It is a bounded source intended for offline
synchronization; it does not poll for changes or implement CDC.

## Options

| Option | Required | Default | Description |
| --- | --- | --- | --- |
| `url` | yes | — | JDBC connection URL. |
| `query` | yes | — | SQL query to read. |
| `username` | no | empty | JDBC user name. |
| `password` | no | empty | JDBC password. |
| `driver` | no | — | JDBC driver class to load before connecting. |
| `fetch-size` | no | `1000` | Maximum number of rows emitted in one batch. |

The result columns retain their query order and use JDBC column labels as the
`FluxRow` field names.
