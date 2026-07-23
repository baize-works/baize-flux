package com.baize.flux.api.factory;

import com.baize.flux.api.configuration.OptionRule;
import com.baize.flux.api.configuration.ReadonlyConfig;
import com.baize.flux.api.table.FluxRow;

public interface SourceFactory {
    String factoryIdentifier();

    OptionRule optionRule();

}
