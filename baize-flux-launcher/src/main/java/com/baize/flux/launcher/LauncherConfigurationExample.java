package com.baize.flux.launcher;

import com.baize.flux.api.configuration.ConfigValidationException;
import com.baize.flux.api.configuration.ConfigValidator;
import com.baize.flux.api.configuration.Constraints;
import com.baize.flux.api.configuration.Option;
import com.baize.flux.api.configuration.OptionRule;
import com.baize.flux.api.configuration.Options;
import com.baize.flux.api.configuration.ReadonlyConfig;
import com.baize.flux.api.configuration.ValidationResult;
import com.baize.flux.framework.configuration.ConfigParseException;
import com.baize.flux.framework.configuration.HoconConfigLoader;

import java.time.Duration;
import java.util.Map;

/**
 * Baize Flux 配置模块使用示例。
 *
 * 演示以下流程：
 *
 * 1. 生成 HOCON 字符串
 * 2. 解析 HOCON
 * 3. 校验作业级配置
 * 4. 校验 source、sink 连接器配置
 * 5. 类型安全地读取配置
 */
public final class LauncherConfigurationExample {

    /*
     * ============================================================
     * 作业级配置项
     * ============================================================
     */

    private static final Option<String> JOB_NAME =
            Options.key("job.name")
                    .stringType()
                    .description("作业名称")
                    .noDefaultValue();

    private static final Option<Integer> BATCH_SIZE =
            Options.key("runtime.batch-size")
                    .intType()
                    .description("单次批处理数据量")
                    .defaultValue(1000);

    private static final Option<Duration> EXECUTION_TIMEOUT =
            Options.key("runtime.execution-timeout")
                    .durationType()
                    .description("作业执行超时时间")
                    .defaultValue(Duration.ofMinutes(30));

    private static final Option<String> SOURCE_TYPE =
            Options.key("source.type")
                    .stringType()
                    .description("Source 连接器类型")
                    .noDefaultValue();

    private static final Option<Map<String, Object>> SOURCE_OPTIONS =
            Options.key("source.options")
                    .mapObjectType()
                    .description("Source 连接器配置")
                    .noDefaultValue();

    private static final Option<String> SINK_TYPE =
            Options.key("sink.type")
                    .stringType()
                    .description("Sink 连接器类型")
                    .noDefaultValue();

    private static final Option<Map<String, Object>> SINK_OPTIONS =
            Options.key("sink.options")
                    .mapObjectType()
                    .description("Sink 连接器配置")
                    .noDefaultValue();

    /**
     * 作业级配置规则。
     */
    private static final OptionRule JOB_RULE =
            OptionRule.builder()
                    .required(
                            JOB_NAME,
                            SOURCE_TYPE,
                            SOURCE_OPTIONS,
                            SINK_TYPE,
                            SINK_OPTIONS
                    )
                    .optional(
                            BATCH_SIZE,
                            EXECUTION_TIMEOUT
                    )
                    .constrain(
                            JOB_NAME,
                            Constraints.notBlank()
                    )
                    .constrain(
                            SOURCE_TYPE,
                            Constraints.notBlank()
                    )
                    .constrain(
                            SINK_TYPE,
                            Constraints.notBlank()
                    )
                    .constrain(
                            BATCH_SIZE,
                            Constraints.between(1, 100000)
                    )
                    .constrain(
                            SOURCE_OPTIONS,
                            Constraints.notEmptyMap()
                    )
                    .constrain(
                            SINK_OPTIONS,
                            Constraints.notEmptyMap()
                    )
                    .build();

    /*
     * ============================================================
     * JDBC Source 配置项
     *
     * 正式代码中建议放到 JDBC Connector 模块，不要长期留在 launcher。
     * ============================================================
     */

    private static final Option<String> JDBC_SOURCE_URL =
            Options.key("url")
                    .stringType()
                    .description("JDBC 连接地址")
                    .noDefaultValue();

    private static final Option<String> JDBC_SOURCE_USERNAME =
            Options.key("username")
                    .stringType()
                    .description("数据库用户名")
                    .defaultValue("");

    private static final Option<String> JDBC_SOURCE_PASSWORD =
            Options.key("password")
                    .stringType()
                    .description("数据库密码")
                    .sensitive()
                    .defaultValue("");

    private static final Option<String> JDBC_SOURCE_QUERY =
            Options.key("query")
                    .stringType()
                    .description("Source 查询 SQL")
                    .noDefaultValue();

    private static final Option<Integer> JDBC_FETCH_SIZE =
            Options.key("fetch-size")
                    .intType()
                    .description("JDBC Fetch Size")
                    .defaultValue(1000);

    private static final OptionRule JDBC_SOURCE_RULE =
            OptionRule.builder()
                    .required(
                            JDBC_SOURCE_URL,
                            JDBC_SOURCE_QUERY
                    )
                    .optional(
                            JDBC_SOURCE_USERNAME,
                            JDBC_SOURCE_PASSWORD,
                            JDBC_FETCH_SIZE
                    )
                    .constrain(
                            JDBC_SOURCE_URL,
                            Constraints.notBlank()
                    )
                    .constrain(
                            JDBC_SOURCE_QUERY,
                            Constraints.notBlank()
                    )
                    .constrain(
                            JDBC_FETCH_SIZE,
                            Constraints.greaterOrEqual(1)
                    )
                    .build();

    /*
     * ============================================================
     * JDBC Sink 配置项
     * ============================================================
     */

    private static final Option<String> JDBC_SINK_URL =
            Options.key("url")
                    .stringType()
                    .description("JDBC 连接地址")
                    .noDefaultValue();

    private static final Option<String> JDBC_SINK_USERNAME =
            Options.key("username")
                    .stringType()
                    .description("数据库用户名")
                    .defaultValue("");

    private static final Option<String> JDBC_SINK_PASSWORD =
            Options.key("password")
                    .stringType()
                    .description("数据库密码")
                    .sensitive()
                    .defaultValue("");

    private static final Option<String> JDBC_SINK_TABLE =
            Options.key("table")
                    .stringType()
                    .description("目标表名称")
                    .noDefaultValue();

    private static final Option<Integer> JDBC_BATCH_SIZE =
            Options.key("batch-size")
                    .intType()
                    .description("JDBC 写入批次大小")
                    .defaultValue(1000);

    private static final OptionRule JDBC_SINK_RULE =
            OptionRule.builder()
                    .required(
                            JDBC_SINK_URL,
                            JDBC_SINK_TABLE
                    )
                    .optional(
                            JDBC_SINK_USERNAME,
                            JDBC_SINK_PASSWORD,
                            JDBC_BATCH_SIZE
                    )
                    .constrain(
                            JDBC_SINK_URL,
                            Constraints.notBlank()
                    )
                    .constrain(
                            JDBC_SINK_TABLE,
                            Constraints.notBlank()
                    )
                    .constrain(
                            JDBC_BATCH_SIZE,
                            Constraints.greaterOrEqual(1)
                    )
                    .build();

    private LauncherConfigurationExample() {
    }

    public static void main(String[] args) {
        String hocon = buildHocon();

        System.out.println("============== HOCON ==============");
        System.out.println(hocon);

        try {
            ReadonlyConfig configuration =
                    parseAndValidate(hocon);

            System.out.println();
            System.out.println("============== 解析结果 ==============");
            System.out.println("job.name = "
                    + configuration.get(JOB_NAME));

            System.out.println("runtime.batch-size = "
                    + configuration.get(BATCH_SIZE));

            System.out.println("runtime.execution-timeout = "
                    + configuration.get(EXECUTION_TIMEOUT));

            System.out.println("source.type = "
                    + configuration.get(SOURCE_TYPE));

            System.out.println("sink.type = "
                    + configuration.get(SINK_TYPE));

            ReadonlyConfig sourceConfig =
                    ReadonlyConfig.fromMap(
                            configuration.get(SOURCE_OPTIONS)
                    );

            System.out.println("source.url = "
                    + sourceConfig.get(JDBC_SOURCE_URL));

            System.out.println("source.query = "
                    + sourceConfig.get(JDBC_SOURCE_QUERY));

            ReadonlyConfig sinkConfig =
                    ReadonlyConfig.fromMap(
                            configuration.get(SINK_OPTIONS)
                    );

            System.out.println("sink.url = "
                    + sinkConfig.get(JDBC_SINK_URL));

            System.out.println("sink.table = "
                    + sinkConfig.get(JDBC_SINK_TABLE));

        } catch (ConfigParseException e) {
            System.err.println("HOCON 解析失败："
                    + e.getMessage());

        } catch (ConfigValidationException e) {
            printValidationResult(
                    e.validationResult()
            );
        }
    }

    /**
     * 生成一份 HOCON 配置字符串。
     */
    public static String buildHocon() {
        return String.join(
                System.lineSeparator(),
                "job {",
                "  name = \"mysql-to-mysql\"",
                "}",
                "",
                "runtime {",
                "  batch-size = 1000",
                "  execution-timeout = \"30m\"",
                "}",
                "",
                "source {",
                "  type = \"jdbc\"",
                "",
                "  options {",
                "    url = \"jdbc:mysql://localhost:3306/source_db\"",
                "    username = \"root\"",
                "    password = \"123456\"",
                "    query = \"select id, name from users\"",
                "    fetch-size = 1000",
                "  }",
                "}",
                "",
                "sink {",
                "  type = \"jdbc\"",
                "",
                "  options {",
                "    url = \"jdbc:mysql://localhost:3306/target_db\"",
                "    username = \"root\"",
                "    password = \"123456\"",
                "    table = \"users_copy\"",
                "    batch-size = 1000",
                "  }",
                "}"
        );
    }

    /**
     * 解析并校验完整配置。
     */
    public static ReadonlyConfig parseAndValidate(
            String hocon) {

        HoconConfigLoader loader =
                new HoconConfigLoader();

        /*
         * 第一阶段：检查 HOCON 语法。
         */
        ReadonlyConfig rootConfig =
                loader.parse(hocon);

        /*
         * 第二阶段：校验作业整体结构。
         */
        validate(
                "job",
                rootConfig,
                JOB_RULE
        );

        /*
         * 第三阶段：校验 Source 连接器配置。
         */
        String sourceType =
                rootConfig.get(SOURCE_TYPE);

        ReadonlyConfig sourceConfig =
                ReadonlyConfig.fromMap(
                        rootConfig.get(SOURCE_OPTIONS)
                );

        if ("jdbc".equalsIgnoreCase(sourceType)) {
            validate(
                    "source.options",
                    sourceConfig,
                    JDBC_SOURCE_RULE
            );
        } else {
            throw new IllegalArgumentException(
                    "Unsupported source type: "
                            + sourceType
            );
        }

        /*
         * 第四阶段：校验 Sink 连接器配置。
         */
        String sinkType =
                rootConfig.get(SINK_TYPE);

        ReadonlyConfig sinkConfig =
                ReadonlyConfig.fromMap(
                        rootConfig.get(SINK_OPTIONS)
                );

        if ("jdbc".equalsIgnoreCase(sinkType)) {
            validate(
                    "sink.options",
                    sinkConfig,
                    JDBC_SINK_RULE
            );
        } else {
            throw new IllegalArgumentException(
                    "Unsupported sink type: "
                            + sinkType
            );
        }

        return rootConfig;
    }

    /**
     * 执行严格模式配置校验。
     */
    private static void validate(
            String scope,
            ReadonlyConfig configuration,
            OptionRule rule) {

        ValidationResult result =
                ConfigValidator.strict()
                        .validate(
                                configuration,
                                rule
                        );

        if (result.isInvalid()) {
            System.err.println(
                    "配置区域校验失败："
                            + scope
            );

            printValidationResult(result);
        }

        /*
         * 校验失败时抛出 ConfigValidationException。
         */
        result.throwIfInvalid();
    }

    /**
     * 输出详细校验错误。
     */
    private static void printValidationResult(
            ValidationResult result) {

        System.err.println(
                "配置校验失败，共发现 "
                        + result.violationCount()
                        + " 个问题："
        );

        for (ValidationResult.Violation violation
                : result.violations()) {

            System.err.println(
                    "  - type="
                            + violation.type()
                            + ", options="
                            + violation.optionKeys()
                            + ", message="
                            + violation.message()
            );
        }
    }
}