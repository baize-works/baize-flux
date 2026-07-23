# exception

## 当前结构

```text
exception
├── CommonErrorCode.java
├── ErrorCategory.java
├── FluxErrorCode.java
└── FluxException.java
````

* `FluxException`：统一异常类型
* `FluxErrorCode`：统一错误码接口
* `ErrorCategory`：错误分类
* `CommonErrorCode`：公共模块错误码

## 核心思想

异常统一使用：

```text
FluxException + FluxErrorCode
```

每个异常包含：

```text
错误码
错误描述
错误分类
详细信息
原始异常
上下文信息
```

错误码由产生错误的模块维护，不集中堆放在 `common`。

```text
COMMON-xxx   公共错误
CORE-xxx     执行引擎错误
JDBC-xxx     JDBC 错误
FILE-xxx     文件连接器错误
```

包装底层异常时必须保留 `cause`。

已经是 `FluxException` 的异常，不重复包装。

## 后续扩展

各模块通过实现 `FluxErrorCode` 定义自己的错误码：

```java
public enum CoreErrorCode implements FluxErrorCode {
    // ...
}
```

后续可基于错误码和错误分类扩展：

```text
任务重试
错误统计
日志聚合
告警通知
Web 页面错误展示
```

重试策略和异常处理流程由执行引擎负责，不放在 `common`。

