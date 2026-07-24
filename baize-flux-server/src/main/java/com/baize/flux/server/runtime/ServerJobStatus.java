package com.baize.flux.server.runtime;
/** 服务异步生命周期；与框架最终状态分离，避免暴露 CREATED。 */
public enum ServerJobStatus { SUBMITTED, RUNNING, CANCELLING, CANCELED, SUCCEEDED, FAILED }
