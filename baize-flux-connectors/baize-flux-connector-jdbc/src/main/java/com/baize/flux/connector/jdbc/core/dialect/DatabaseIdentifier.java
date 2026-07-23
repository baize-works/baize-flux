package com.baize.flux.connector.jdbc.core.dialect;

/**
 * JDBC 数据库方言标识。
 *
 * 标识统一使用小写，便于配置和 SPI 匹配。
 */
public final class DatabaseIdentifier {

    public static final String MYSQL = "mysql";

    private DatabaseIdentifier() {}
}
