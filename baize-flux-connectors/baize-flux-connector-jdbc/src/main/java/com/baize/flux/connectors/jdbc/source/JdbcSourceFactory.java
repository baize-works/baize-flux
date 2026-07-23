package com.baize.flux.connectors.jdbc.source;

import com.baize.flux.api.configuration.*;
import com.baize.flux.api.factory.SourceFactory;
import com.baize.flux.api.source.BoundedSource;
import com.baize.flux.api.table.FluxRow;

/** Creates a finite JDBC query source for offline synchronization. */
public final class JdbcSourceFactory implements SourceFactory {
    static final Option<String> URL = Options.key("url").stringType().noDefaultValue();
    static final Option<String> QUERY = Options.key("query").stringType().noDefaultValue();
    static final Option<String> USERNAME = Options.key("username").stringType().defaultValue("");
    static final Option<String> PASSWORD = Options.key("password").stringType().sensitive().defaultValue("");
    static final Option<String> DRIVER = Options.key("driver").stringType().noDefaultValue();
    static final Option<Integer> FETCH_SIZE = Options.key("fetch-size").intType().defaultValue(1_000);

    private static final OptionRule OPTION_RULE = OptionRule.builder()
            .required(URL, QUERY)
            .optional(USERNAME, PASSWORD, DRIVER, FETCH_SIZE)
            .constrain(URL, Constraints.notBlank())
            .constrain(QUERY, Constraints.notBlank())
            .constrain(FETCH_SIZE, Constraints.greaterOrEqual(1))
            .build();

    @Override
    public String factoryIdentifier() {
        return "jdbc";
    }

    @Override
    public OptionRule optionRule() {
        return OPTION_RULE;
    }

    @Override
    public BoundedSource<FluxRow, ?> createSource(ReadonlyConfig config) {
        return new JdbcSource(
                config.get(URL), config.get(QUERY), config.get(USERNAME),
                config.get(PASSWORD), config.getResolvedOptional(DRIVER).orElse(null),
                config.get(FETCH_SIZE));
    }
}
