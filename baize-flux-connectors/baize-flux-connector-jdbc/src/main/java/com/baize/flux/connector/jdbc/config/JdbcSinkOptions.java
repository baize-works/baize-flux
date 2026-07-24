package com.baize.flux.connector.jdbc.config;

import com.baize.flux.api.configuration.Option;
import com.baize.flux.api.configuration.Options;
import com.baize.flux.connector.jdbc.sink.DataSaveMode;
import com.baize.flux.connector.jdbc.sink.DirtyDataPolicy;
import com.baize.flux.connector.jdbc.sink.SchemaSaveMode;

import java.util.List;

/**
 * JDBC 离线 Sink 配置项。
 */
public final class JdbcSinkOptions
        extends JdbcCommonOptions {


    /**
     * 目标表路径。
     * <p>
     * 单表：
     * <p>
     * database.table
     * <p>
     * 多表可以不配置，默认沿用 Source 表路径。支持
     * {@code ${schema_name}} 和 {@code ${table_name}} 占位符，例如
     * {@code archive.${schema_name}_${table_name}}。
     * <p>
     * 配置目标表时优先使用自动生成的 INSERT/UPSERT SQL，忽略
     * {@code custom_sql}（以及其 {@code query} 别名）。
     */
    public static final Option<String> TABLE_PATH =
            Options.key("table_path")
                    .stringType()
                    .noDefaultValue()
                    .withFallbackKeys("table")
                    .withDescription("目标表路径或多表目标模板，支持 ${schema_name} 和 ${table_name}");

    public static final Option<SchemaSaveMode>
            SCHEMA_SAVE_MODE =
            Options.key("schema_save_mode")
                    .enumType(
                            SchemaSaveMode.class)
                    .defaultValue(
                            SchemaSaveMode
                                    .CREATE_SCHEMA_WHEN_NOT_EXIST)
                    .withDescription("目标表结构处理方式");

    public static final Option<DataSaveMode>
            DATA_SAVE_MODE =
            Options.key("data_save_mode")
                    .enumType(
                            DataSaveMode.class)
                    .defaultValue(
                            DataSaveMode.APPEND_DATA)
                    .withDescription("目标表已有数据处理方式");

    /**
     * 未配置时，由 JdbcDialect 自动生成 INSERT/UPSERT SQL。
     */
    public static final Option<String> CUSTOM_SQL =
            Options.key("custom_sql")
                    .stringType()
                    .noDefaultValue()
                    .withFallbackKeys("query")
                    .withDescription("自定义写入 SQL");

    public static final Option<JdbcWriteMode>
            WRITE_MODE =
            Options.key("write_mode")
                    .enumType(
                            JdbcWriteMode.class)
                    .defaultValue(
                            JdbcWriteMode.INSERT)
                    .withDescription("INSERT 或 UPSERT");

    /**
     * 用户显式指定的主键。
     * <p>
     * 未配置时可以使用 CatalogTable 中发现的主键。
     */
    public static final Option<List<String>>
            PRIMARY_KEYS =
            Options.key("primary_keys")
                    .listType()
                    .noDefaultValue()
                    .withDescription("UPSERT 使用的主键字段");

    public static final Option<Integer> BATCH_SIZE =
            Options.key("batch_size")
                    .intType()
                    .defaultValue(1000)
                    .withDescription("JDBC executeBatch 每批写入数量");

    public static final Option<Integer> MAX_RETRIES =
            Options.key("max_retries")
                    .intType()
                    .defaultValue(3)
                    .withDescription("批次写入失败后的最大重试次数");

    /**
     * 脏数据处理策略。SKIP 会使用 Savepoint 回滚失败批次，然后逐行定位并跳过
     * 无法写入的记录；FAIL_FAST 保持事务失败即回滚的默认行为。
     */
    public static final Option<DirtyDataPolicy> DIRTY_DATA_POLICY =
            Options.key("dirty_data_policy")
                    .enumType(DirtyDataPolicy.class)
                    .defaultValue(DirtyDataPolicy.FAIL_FAST)
                    .withDescription("脏数据处理策略：FAIL_FAST 或 SKIP");

    /**
     * 自动创建目标表时是否创建主键。
     * <p>
     * 当前 Catalog 第一版只支持主键，不处理普通索引。
     */
    public static final Option<Boolean>
            CREATE_PRIMARY_KEY =
            Options.key("create_primary_key")
                    .booleanType()
                    .defaultValue(true)
                    .withDescription("自动建表时是否创建主键");
}
