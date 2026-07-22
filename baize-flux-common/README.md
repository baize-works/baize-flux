# baize-flux-common

`baize-flux-common` 是 Baize Flux 的基础公共模块，用于存放各模块都会使用的通用代码。

该模块不包含数据同步业务逻辑，也不负责执行任务。

baize-flux-common
└── src/main/java/com/baize/flux/common
├── constant
├── enums
├── exception
├── model
└── util


exception
baize-flux-core
└── exception
└── CoreErrorCode.java

baize-flux-connector-jdbc
└── exception
└── JdbcErrorCode.java