package com.baize.flux.api.table.catalog;

import com.baize.flux.api.table.catalog.exception.CatalogException;
import com.baize.flux.api.table.catalog.exception.DatabaseAlreadyExistsException;
import com.baize.flux.api.table.catalog.exception.DatabaseNotFoundException;
import com.baize.flux.api.table.catalog.exception.TableAlreadyExistsException;
import com.baize.flux.api.table.catalog.exception.TableNotFoundException;

/**
 * 支持 DDL 操作的 Catalog。
 *
 * Source 只依赖 Catalog；
 * 需要自动建表的 Sink 才依赖 WritableCatalog。
 */
public interface WritableCatalog extends Catalog {

    /**
     * 创建数据库。
     */
    void createDatabase(
            String databaseName,
            boolean ignoreIfExists)
            throws CatalogException,
            DatabaseAlreadyExistsException;

    /**
     * 删除数据库。
     */
    void dropDatabase(
            String databaseName,
            boolean ignoreIfNotExists)
            throws CatalogException,
            DatabaseNotFoundException;

    /**
     * 创建表。
     *
     * 表路径直接从 CatalogTable 中获取，
     * 避免同时传入 TablePath 和 CatalogTable 导致不一致。
     */
    void createTable(
            CatalogTable table,
            boolean ignoreIfExists)
            throws CatalogException,
            DatabaseNotFoundException,
            TableAlreadyExistsException;

    /**
     * 删除表。
     */
    void dropTable(
            TablePath tablePath,
            boolean ignoreIfNotExists)
            throws CatalogException,
            TableNotFoundException;

    /**
     * 清空表数据，但保留表结构。
     */
    void truncateTable(
            TablePath tablePath,
            boolean ignoreIfNotExists)
            throws CatalogException,
            TableNotFoundException;
}