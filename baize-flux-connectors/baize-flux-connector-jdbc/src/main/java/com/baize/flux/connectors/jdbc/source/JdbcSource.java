package com.baize.flux.connectors.jdbc.source;

import com.baize.flux.api.source.*;


public class JdbcSource
        implements FluxSource {


    private final JdbcSourceConfig config;



    public JdbcSource(
            JdbcSourceConfig config){

        this.config=config;
    }



    @Override
    public SourceReader createReader(){

        return new JdbcSourceReader(config);
    }

}
