package com.baize.flux.api.sink;

/**
 * The scope at which a sink can make data durable.
 */
public enum CommitScope {

    /**
     * Each sink task commits independently. This does not provide Job-level atomicity.
     */
    TASK_LOCAL
}
