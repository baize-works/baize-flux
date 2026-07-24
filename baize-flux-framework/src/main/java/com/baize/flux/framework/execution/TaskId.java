package com.baize.flux.framework.execution;

/**
 * @deprecated 请使用 API 的 {@link com.baize.flux.api.sink.TaskId}。
 */
@Deprecated
public final class TaskId extends com.baize.flux.api.sink.TaskId {
    public TaskId(String stageName, int subtaskIndex, int parallelism) {
        super(stageName, subtaskIndex, parallelism);
    }
}
