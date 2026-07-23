package com.baize.flux.connectors.jdbc.source;


import com.baize.flux.api.configuration.ReadonlyConfig;

import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * JDBC source runtime configuration.
 *
 * <p>
 * This class is created from {@link ReadonlyConfig}
 * and contains all JDBC source runtime parameters.
 */
@Getter
@Builder
public class JdbcSourceConfig {


    /**
     * JDBC connection url.
     */
    private final String url;



    /**
     * JDBC driver class.
     */
    private final String driver;



    /**
     * Database username.
     */
    private final String username;



    /**
     * Database password.
     */
    private final String password;



    /**
     * Custom query sql.
     */
    private final String sql;



    /**
     * Fetch size.
     */
    @Builder.Default
    private final int fetchSize = 1000;



    /**
     * Connection timeout milliseconds.
     */
    @Builder.Default
    private final long connectionTimeoutMs = 30000L;



    /**
     * Maximum connection retry count.
     */
    @Builder.Default
    private final int maxRetries = 3;



    /**
     * Table names.
     */
    @Builder.Default
    private final List<String> tables =
            Collections.emptyList();



    /**
     * JDBC properties.
     */
    @Builder.Default
    private final Map<String,String> properties =
            Collections.emptyMap();




    /**
     * Create JDBC configuration from generic config.
     *
     * @param config readonly configuration
     * @return jdbc source configuration
     */
    public static JdbcSourceConfig of(
            ReadonlyConfig config){


        return JdbcSourceConfig.builder()

                .url(
                        config.get(
                                JdbcSourceOptions.URL
                        )
                )

                .driver(
                        config.get(
                                JdbcSourceOptions.DRIVER
                        )
                )

                .username(
                        config.get(
                                JdbcSourceOptions.USERNAME
                        )
                )

                .password(
                        config.get(
                                JdbcSourceOptions.PASSWORD
                        )
                )


                .build();

    }


}
