package com.baize.flux.connectors.jdbc.source;


import com.baize.flux.api.configuration.util.OptionRule;
import com.baize.flux.api.source.FluxSource;
import com.baize.flux.api.source.SourceFactory;
import com.baize.flux.api.source.SourceFactoryContext;


public class JdbcSourceFactory
        implements SourceFactory {


    @Override
    public String factoryIdentifier() {

        return "jdbc";
    }


    @Override
    public FluxSource createSource(
            SourceFactoryContext context) {


        JdbcSourceConfig config =
                JdbcSourceConfig.of(
                        context.getConfig()
                );


        return new JdbcSource(config);
    }


    @Override
    public OptionRule optionRule() {


        return OptionRule.builder()

                .required(
                        JdbcSourceOptions.URL,
                        JdbcSourceOptions.DRIVER
                )

                .optional(
                        JdbcSourceOptions.USERNAME,
                        JdbcSourceOptions.PASSWORD
                )

                .build();
    }

}