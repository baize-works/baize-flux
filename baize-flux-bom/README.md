# baize-flux-bom

`baize-flux-bom` 用于统一管理 Baize Flux 项目中的第三方依赖版本，避免不同模块重复声明版本或产生依赖冲突。

## 使用方式

在其他项目中使用 Baize Flux 模块时，可以在 `dependencyManagement` 中导入 BOM：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.baize.flux</groupId>
            <artifactId>baize-flux-bom</artifactId>
            <version>${baize-flux.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>