package com.baize.flux.connector.jdbc.source;

import com.baize.flux.api.configuration.ReadonlyConfig;
import com.baize.flux.api.configuration.util.OptionRule;
import com.baize.flux.api.source.SourceFactory;
import com.baize.flux.api.source.SourceFactoryContext;
import com.baize.flux.api.table.catalog.CatalogTable;
import com.baize.flux.api.table.factory.TableSourceFactory;
import com.baize.flux.connector.jdbc.config.JdbcCommonOptions;
import com.baize.flux.connector.jdbc.config.JdbcSourceConfig;
import com.baize.flux.connector.jdbc.config.JdbcSourceOptions;
import com.baize.flux.connector.jdbc.core.dialect.JdbcDialect;
import com.baize.flux.connector.jdbc.core.dialect.JdbcDialectLoader;
import com.baize.flux.connector.jdbc.options.MultiTableCommonOptions;

import com.baize.flux.connector.jdbc.utils.JdbcCatalogUtils;
import com.google.auto.service.AutoService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * JDBC Source 工厂。
 *
 * 主要负责：
 *
 * 1. 解析并校验 Source 配置；
 * 2. 校验数据库方言是否可用；
 * 3. 创建 JDBC Source；
 * 4. 发现源表结构。
 *
 * 表读取、分片生成和连接管理不在 Factory 中处理。
 */
@AutoService(SourceFactory.class)
public final class JdbcSourceFactory
        implements TableSourceFactory<JdbcSourceSplit> {

    private static final String IDENTIFIER = "jdbc";

    @Override
    public String factoryIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public JdbcSource createSource(
            SourceFactoryContext context)
            throws Exception {

        JdbcSourceConfig config =
                createConfig(context);

        /*
         * Factory 阶段只校验方言能否成功加载，
         * 不在这里建立连接或修改连接参数。
         */
        loadDialect(config);

        return new JdbcSource(config);
    }

    @Override
    public List<CatalogTable> discoverTableSchemas(
            SourceFactoryContext context)
            throws Exception {

        JdbcSourceConfig config =
                createConfig(context);

        JdbcDialect dialect =
                loadDialect(config);

        Map<?, JdbcSourceTable> tables =
                JdbcCatalogUtils.getTables(config, dialect);

        List<CatalogTable> result =
                new ArrayList<>(tables.size());

        for (JdbcSourceTable table : tables.values()) {
            result.add(table.getCatalogTable());
        }

        return Collections.unmodifiableList(result);
    }

    @Override
    public OptionRule optionRule() {
        /*
         * URL、Driver、用户名、密码和连接属性，
         * 统一复用 JDBC 公共连接规则。
         *
         * table_list 与 table_path 的互斥关系，
         * 由 JdbcSourceTableConfig.from() 统一校验。
         */
        return JdbcCommonOptions.baseConnectionRule()
                .optional(
                        JdbcSourceOptions.TABLE_LIST,
                        JdbcSourceOptions.TABLE_PATH,
                        JdbcSourceOptions.QUERY,
                        JdbcSourceOptions.WHERE_CONDITION,
                        JdbcSourceOptions.FETCH_SIZE,
                        JdbcSourceOptions.PARTITION_COLUMN,
                        JdbcSourceOptions.PARTITION_LOWER_BOUND,
                        JdbcSourceOptions.PARTITION_UPPER_BOUND,
                        JdbcSourceOptions.PARTITION_NUM,
                        MultiTableCommonOptions
                                .MULTI_TABLE_FAILURE_POLICY)
                .build();
    }

    /**
     * 创建并校验 JDBC Source 配置。
     */
    private JdbcSourceConfig createConfig(
            SourceFactoryContext context) {

        Objects.requireNonNull(
                context,
                "context must not be null");

        ReadonlyConfig options =
                Objects.requireNonNull(
                        context.getOptions(),
                        "source options must not be null");

        return JdbcSourceConfig.of(options);
    }

    /**
     * 加载当前数据库方言。
     *
     * 这里只完成方言识别，不修改配置，也不建立数据库连接。
     */
    private JdbcDialect loadDialect(
            JdbcSourceConfig config) {

        return JdbcDialectLoader.load(
                config.getConnectionConfig());
    }
}
