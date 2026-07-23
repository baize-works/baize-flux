package com.baize.flux.api.source;


import com.baize.flux.api.configuration.util.OptionRule;

public interface SourceFactory {


    String factoryIdentifier();



    FluxSource createSource(
            SourceFactoryContext context);



    OptionRule optionRule();

}