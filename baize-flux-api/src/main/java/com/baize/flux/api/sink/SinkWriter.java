package com.baize.flux.api.sink;

import com.baize.flux.api.source.RecordBatch;
import com.baize.flux.api.table.catalog.CatalogTable;

/**
 * 一个 SinkTask 对应一个 SinkWriter。
 *
 * <p>事务生命周期：
 *
 * <pre>
 * open
 *   -> write
 *   -> write
 *   -> prepareCommit
 *   -> commit
 *   -> abort (on failure before commit)
 *   -> close
 * </pre>
 */
public interface SinkWriter<T> extends AutoCloseable {

    /**
     * 初始化 Writer。
     *
     * <p>事务型 Sink 可在这里初始化事务上下文；
     * 非事务型 Sink 可以使用默认实现。
     */
    default void open() throws Exception {
    }

    /**
     * 写入一批数据。
     *
     * <p>调用 write 成功不代表数据已经最终提交。
     */
    void write(
            RecordBatch<T> batch,
            CatalogTable sourceTable)
            throws Exception;

    /** Called after all writes succeed and before the task-local commit boundary. */
    default void prepareCommit() throws Exception {
    }

    /** Commits this writer at {@link #getCommitScope()}; it is not necessarily a Job commit. */
    default void commit() throws Exception {
    }

    /** Aborts uncommitted work after a write or prepare/commit failure. */
    default void abort() throws Exception {
        rollback();
    }

    /**
     * Legacy rollback hook. New writers should implement {@link #abort()}.
     */
    @Deprecated
    default void rollback() throws Exception {
    }

    /**
     * Returns the strongest commit scope provided by this writer.
     *
     * <p>The default is task-local: independently committing JDBC-style writers do not
     * guarantee Job-level atomicity or a global rollback.
     */
    default CommitScope getCommitScope() {
        return CommitScope.TASK_LOCAL;
    }

    /** User-facing advice for safely rerunning a failed job, if the connector can provide it. */
    default String getRetryAdvice() {
        return "This sink commits per task; verify already committed target data before rerunning.";
    }

    /**
     * 释放 Writer 持有的资源。
     *
     * <p>close 不负责提交事务。
     */
    @Override
    void close() throws Exception;
}
