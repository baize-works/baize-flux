package com.baize.flux.api.source;


import com.baize.flux.api.configuration.ReadonlyConfig;

public class SourceFactoryContext {


    private final ReadonlyConfig config;



    public SourceFactoryContext(
            ReadonlyConfig config){

        this.config = config;
    }



    public ReadonlyConfig getConfig(){

        return config;
    }

}