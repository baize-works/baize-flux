package com.baize.flux.connector.jdbc.catalog.mysql;

import com.baize.flux.api.configuration.ReadonlyConfig;
import com.baize.flux.api.factory.Factory;
import com.baize.flux.api.table.catalog.Catalog;
import com.baize.flux.api.table.factory.CatalogFactory;
import com.baize.flux.connector.jdbc.catalog.JdbcCatalogConfig;
import com.baize.flux.connector.jdbc.config.JdbcCommonOptions;
import com.google.auto.service.AutoService;

import java.util.Collections;
import java.util.Map;

/**
 * MySQL Catalog 工厂。
 */
@AutoService(Factory.class)
public final class MySqlCatalogFactory
        implements CatalogFactory {

    private static final String IDENTIFIER = "mysql";

    private static final String DEFAULT_DRIVER =
            "com.mysql.cj.jdbc.Driver";

    @Override
    public String factoryIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public Catalog createCatalog(
            String catalogName,
            ReadonlyConfig options) {

        String url =
                options.get(JdbcCommonOptions.URL);

        String username =
                options.getOptional(
                        JdbcCommonOptions.USERNAME)
                        .orElse(null);

        String password =
                options.getOptional(
                        JdbcCommonOptions.PASSWORD)
                        .orElse(null);

        String driver =
                options.getOptional(
                        JdbcCommonOptions.DRIVER)
                        .orElse(DEFAULT_DRIVER);

        Map<String, String> properties =
                options.getOptional(
                        JdbcCommonOptions.PROPERTIES)
                        .orElse(Collections.emptyMap());

        boolean intTypeNarrowing =
                options.getOptional(
                        JdbcCommonOptions.INT_TYPE_NARROWING)
                        .orElse(false);

        JdbcCatalogConfig config =
                new JdbcCatalogConfig(
                        url,
                        username,
                        password,
                        driver,
                        properties,
                        intTypeNarrowing);

        return new MySqlCatalog(
                catalogName,
                config);
    }

}