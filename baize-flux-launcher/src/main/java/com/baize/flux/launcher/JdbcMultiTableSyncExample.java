package com.baize.flux.launcher;

/**
 * Runnable JDBC multi-table synchronization example.
 *
 * <p>Update the connection properties and source table names before running
 * this class. Pass a complete HOCON job as the sole argument to override the
 * example configuration.
 */
public final class JdbcMultiTableSyncExample {

    private static final String EXAMPLE_HOCON =
            "source {\n"
                    + "  type = \"jdbc\"\n"
                    + "  batch-size = 1000\n"
                    + "  url = \"jdbc:mysql://127.0.0.1:3306/flux_test?useSSL=false&serverTimezone=UTC\"\n"
                    + "  driver = \"com.mysql.cj.jdbc.Driver\"\n"
                    + "  user = \"root\"\n"
                    + "  password = \"123456\"\n"
                    + "  table_list = [\n"
                    + "    { table_path = \"flux_test.user_info\" },\n"
                    + "    { table_path = \"flux_test.orders\" }\n"
                    + "  ]\n"
                    + "}\n"
                    + "sink {\n"
                    + "  type = \"jdbc\"\n"
                    + "  url = \"jdbc:mysql://127.0.0.1:3306/test1?useSSL=false&serverTimezone=UTC\"\n"
                    + "  driver = \"com.mysql.cj.jdbc.Driver\"\n"
                    + "  user = \"root\"\n"
                    + "  password = \"123456\"\n"
                    + "  # `table` is an alias for table_path. Keep the target database explicit:\n"
                    + "  # `${schema_name}` would resolve to the source database (flux_test), not test1.\n"
                    + "  table = \"test1.sink_${table_name}\"\n"
                    + "  schema_save_mode = CREATE_SCHEMA_WHEN_NOT_EXIST\n"
                    + "  data_save_mode = APPEND_DATA\n"
                    + "}\n";

    private JdbcMultiTableSyncExample() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 1) {
            throw new IllegalArgumentException(
                    "Usage: JdbcMultiTableSyncExample [\"<HOCON job configuration>\"]");
        }
        LocalSyncLauncher.run(
                args.length == 1 ? args[0] : EXAMPLE_HOCON,
                Thread.currentThread().getContextClassLoader());
    }
}
