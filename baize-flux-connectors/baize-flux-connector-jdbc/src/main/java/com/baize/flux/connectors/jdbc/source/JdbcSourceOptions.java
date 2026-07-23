package com.baize.flux.connectors.jdbc.source;


import com.baize.flux.api.configuration.Option;
import com.baize.flux.api.configuration.Options;



public final class JdbcSourceOptions {


    public static final Option<String> URL =
            Options.key("url")
                    .stringType()
                    .noDefaultValue()
                    .withDescription(
                            "jdbc url"
                    );



    public static final Option<String> DRIVER =
            Options.key("driver")
                    .stringType()
                    .noDefaultValue();



    public static final Option<String> USERNAME =
            Options.key("username")
                    .stringType()
                    .noDefaultValue();



    public static final Option<String> PASSWORD =
            Options.key("password")
                    .stringType()
                    .noDefaultValue();


}
