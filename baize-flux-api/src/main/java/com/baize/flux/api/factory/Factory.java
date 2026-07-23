package com.baize.flux.api.factory;

import com.baize.flux.api.configuration.util.OptionRule;

/**
 * Flux 插件工厂基础接口。
 *
 * Source、Sink 等插件工厂都需要实现该接口。
 */
public interface Factory {

    /**
     * 插件唯一标识。
     *
     * 建议统一使用小写，例如：
     *
     * jdbc
     * file
     * http
     */
    String factoryIdentifier();

    /**
     * 返回插件配置规则。
     *
     * 用于：
     * 1. 提交任务时校验配置；
     * 2. Web 页面动态生成配置表单。
     */
    OptionRule optionRule();
}