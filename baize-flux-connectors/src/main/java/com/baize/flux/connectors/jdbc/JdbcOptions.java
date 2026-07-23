package com.baize.flux.connectors.jdbc;

import com.baize.flux.api.configuration.Constraints;
import com.baize.flux.api.configuration.Option;
import com.baize.flux.api.configuration.OptionRule;
import com.baize.flux.api.configuration.Options;

final class JdbcOptions {
    static final Option<String> URL = Options.key("url").stringType().noDefaultValue();
    static final Option<String> USERNAME = Options.key("username").stringType().defaultValue("");
    static final Option<String> PASSWORD = Options.key("password").stringType().sensitive().defaultValue("");
    static final Option<Integer> FETCH_SIZE = Options.key("fetch-size").intType().defaultValue(1000);
    static final Option<Integer> BATCH_SIZE = Options.key("batch-size").intType().defaultValue(1000);
    static final Option<String> QUERY = Options.key("query").stringType().noDefaultValue();
    static final Option<String> TABLE = Options.key("table").stringType().noDefaultValue();

    private JdbcOptions() {
    }

    static OptionRule sourceRule() {
        return OptionRule.builder().required(URL, QUERY).optional(USERNAME, PASSWORD, FETCH_SIZE)
                .constrain(URL, Constraints.notBlank())
                .constrain(QUERY, Constraints.notBlank())
                .constrain(FETCH_SIZE, Constraints.greaterOrEqual(1)).build();
    }

    static OptionRule sinkRule() {
        return OptionRule.builder().required(URL, TABLE).optional(USERNAME, PASSWORD, BATCH_SIZE).constrain(URL, Constraints.notBlank()).constrain(TABLE, Constraints.notBlank()).constrain(BATCH_SIZE, Constraints.greaterOrEqual(1)).build();
    }
}
