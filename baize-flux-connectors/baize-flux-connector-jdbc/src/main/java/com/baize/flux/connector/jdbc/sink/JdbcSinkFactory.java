package com.baize.flux.connector.jdbc.sink;


import com.baize.flux.api.configuration.util.OptionRule;
import com.baize.flux.api.factory.SinkFactory;
import com.baize.flux.api.sink.Sink;
import com.baize.flux.api.sink.SinkFactoryContext;
import com.baize.flux.connector.jdbc.config.JdbcCommonOptions;
import com.baize.flux.connector.jdbc.config.JdbcSinkConfig;
import com.baize.flux.connector.jdbc.config.JdbcSinkOptions;
import com.google.auto.service.AutoService;

/**
 * JDBC Sink SPI factory.
 */
@AutoService(SinkFactory.class)
public final class JdbcSinkFactory implements SinkFactory {
    @Override
    public String factoryIdentifier() {
        return "jdbc";
    }

    @Override
    public OptionRule optionRule() {
        return JdbcCommonOptions.baseConnectionRule().optional(
                JdbcSinkOptions.TABLE_PATH, JdbcSinkOptions.SCHEMA_SAVE_MODE,
                JdbcSinkOptions.DATA_SAVE_MODE, JdbcSinkOptions.WRITE_MODE,
                JdbcSinkOptions.CUSTOM_SQL, JdbcSinkOptions.PRIMARY_KEYS,
                JdbcSinkOptions.BATCH_SIZE, JdbcSinkOptions.PREPARED_STATEMENT_CACHE_SIZE,
                JdbcSinkOptions.QUERY_TIMEOUT_SEC, JdbcSinkOptions.MAX_RETRIES,
                JdbcSinkOptions.DIRTY_DATA_POLICY,
                JdbcSinkOptions.CREATE_PRIMARY_KEY).build();
    }

    @Override
    public Sink createSink(SinkFactoryContext context) {
        return new JdbcSink(JdbcSinkConfig.of(context.getOptions()));
    }
}
