package com.baize.flux.connector.jdbc.config;

import com.baize.flux.api.configuration.Option;
import com.baize.flux.api.configuration.Options;

import java.util.List;

/**
 * JDBC 离线 Source 配置项。
 */
public final class JdbcSourceOptions
        extends JdbcCommonOptions {

    /**
     * 数据表路径。
     * <p>
     * 例如：
     * <p>
     * database.table
     * database.schema.table
     */
    public static final Option<String> TABLE_PATH =
            Options.key("table_path")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("源表完整路径");

    /**
     * 自定义查询 SQL。
     * <p>
     * 配置 query 时仍建议配置 table_path，
     * table_path 作为该查询结果的数据集标识。
     */
    public static final Option<String> QUERY =
            Options.key("query")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("自定义查询 SQL");

    /**
     * 多表读取配置。
     */
    public static final Option<List<JdbcSourceTableConfig>>
            TABLE_LIST =
            Options.key("table_list")
                    .listType(
                            JdbcSourceTableConfig.class)
                    .noDefaultValue()
                    .withDescription("多表读取配置");

    /**
     * 公共过滤条件。
     * <p>
     * 推荐只配置条件主体：
     * <p>
     * id > 100
     * <p>
     * 不需要写 where。
     */
    public static final Option<String> WHERE_CONDITION =
            Options.key("where_condition")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("公共查询过滤条件，不需要包含 where");

    /**
     * Requested consistency for the source read.
     */
    public static final Option<ReadConsistency> READ_CONSISTENCY =
            Options.key("read_consistency")
                    .enumType(ReadConsistency.class)
                    .defaultValue(ReadConsistency.BEST_EFFORT)
                    .withDescription("JDBC Source 读取一致性");

    public static final Option<Integer> FETCH_SIZE =
            Options.key("fetch_size")
                    .intType()
                    .defaultValue(1000)
                    .withDescription("JDBC 每次从数据库获取的行数");

    /**
     * 是否启用字段范围分片读取。
     * <p>
     * 默认关闭，保证第一版行为简单、稳定。
     */
    public static final Option<Boolean>
            ENABLE_PARTITION_READ =
            Options.key("enable_partition_read")
                    .booleanType()
                    .defaultValue(false)
                    .withDescription("是否启用分片并行读取");

    public static final Option<String> PARTITION_COLUMN =
            Options.key("partition_column")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("分片字段");

    public static final Option<Integer> PARTITION_NUM =
            Options.key("partition_num")
                    .intType()
                    .defaultValue(4)
                    .withDescription("分片数量");

    public static final Option<String>
            PARTITION_LOWER_BOUND =
            Options.key("partition_lower_bound")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("分片字段下界");

    public static final Option<String>
            PARTITION_UPPER_BOUND =
            Options.key("partition_upper_bound")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("分片字段上界");
}