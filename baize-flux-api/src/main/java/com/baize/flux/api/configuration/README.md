# Baize Flux 配置框架

## 设计目标

配置框架用于统一完成：

* 配置项定义；
* HOCON 解析；
* 类型转换；
* 配置校验；
* 类型安全读取。

## 核心组件

### Option

定义单个配置项，包括名称、类型、默认值和描述。

```
Option<Integer> batchSize =
        Options.key("runtime.batch-size")
                .intType()
                .defaultValue(1000);
```

### ReadonlyConfig

保存解析后的配置，并提供类型安全读取。

```
Integer value = config.get(batchSize);
```

支持点分隔路径，例如：

```hocon
runtime {
  batch-size = 1000
}
```

### OptionRule

定义配置项是否必填以及配置值约束。

```
OptionRule rule =
        OptionRule.builder()
                .required(JOB_NAME)
                .optional(BATCH_SIZE)
                .constrain(BATCH_SIZE, Constraints.greaterOrEqual(1))
                .build();
```

### ConfigValidator

执行配置校验。

```
ConfigValidator.strict()
        .validate(config, rule)
        .throwIfInvalid();
```

严格模式会检查未知配置项、类型错误、必填项和配置约束。

## HOCON 解析

HOCON 解析由 `HoconConfigLoader` 完成。

```
ReadonlyConfig config =
        new HoconConfigLoader()
                .parse(hoconContent);
```

也可以从文件加载：

```
ReadonlyConfig config =
        new HoconConfigLoader()
                .load(configPath);
```

## 配置处理流程

```text
HOCON 字符串或文件
        ↓
HoconConfigLoader
        ↓
ReadonlyConfig
        ↓
ConfigValidator
        ↓
读取配置并创建作业
```

## 分层校验

Launcher 负责校验作业整体配置：

```text
job
runtime
source.type
source.options
sink.type
sink.options
```

具体连接器负责校验自己的参数，例如 JDBC 的：

```text
url
username
password
query
table
```

这样可以避免 Launcher 与具体连接器实现耦合。

## 示例

```
ReadonlyConfig config =
        new HoconConfigLoader().parse(hocon);

ConfigValidator.strict()
        .validate(config, JOB_RULE)
        .throwIfInvalid();

String jobName = config.get(JOB_NAME);
Integer batchSize = config.get(BATCH_SIZE);
```

配置框架的主要职责是：**解析配置、验证配置，并提供统一的类型安全访问方式。**
