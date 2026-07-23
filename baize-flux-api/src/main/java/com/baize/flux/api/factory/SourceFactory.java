package com.baize.flux.api.factory;

import com.baize.flux.api.configuration.util.OptionRule;


/**
 * Source 工厂基础接口。
 */
public interface SourceFactory extends Factory {

    /**
     * Source 配置规则。
     */
    OptionRule optionRule();
}
