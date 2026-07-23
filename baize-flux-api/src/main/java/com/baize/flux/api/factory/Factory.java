package com.baize.flux.api.factory;

import com.baize.flux.api.configuration.util.OptionRule;

/**
 * Flux 插件工厂基础接口。
 *
 * Source、Sink 等插件工厂都需要实现该接口。
 */
public interface Factory {

    /**
     * 工厂唯一标识。
     *
     * 例如：jdbc、mysql-cdc、file。
     */
    String factoryIdentifier();
}