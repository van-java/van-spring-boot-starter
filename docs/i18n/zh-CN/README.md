# van-spring-boot-starter

<p>
  <a href="https://github.com/vanengine/van"><img src="https://img.shields.io/badge/van-template%20engine-steelblue" alt="Van" /></a>
  <a href="https://central.sonatype.com/artifact/dev.vanengine/van-spring-boot-starter"><img src="https://img.shields.io/maven-central/v/dev.vanengine/van-spring-boot-starter" alt="Maven Central" /></a>
  <a href="../../../LICENSE"><img src="https://img.shields.io/badge/license-MIT-blue.svg" alt="License" /></a>
</p>

[Van](https://github.com/vanengine/van) 模板引擎的 Spring Boot 集成 — 使用 Vue SFC 语法编写模板，服务端渲染 HTML，无 Node.js 依赖。

<p>
  <a href="../../../README.md">English</a> · <a href="README.md">简体中文</a>
</p>

## 快速开始

### 1. 添加依赖

**Gradle**

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation 'dev.vanengine:van-spring-boot-starter:0.1.16'
}
```

**Maven**

```xml
<dependency>
    <groupId>dev.vanengine</groupId>
    <artifactId>van-spring-boot-starter</artifactId>
    <version>0.1.16</version>
</dependency>
```

### 2. 创建模板

`src/main/resources/themes/default/pages/index.van`：

```vue
<template>
  <html>
    <body>
      <h1>{{ title }}</h1>
      <p>{{ message }}</p>
    </body>
  </html>
</template>
```

### 3. 返回视图

```java
@Controller
public class IndexController {

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "Hello");
        model.addAttribute("message", "Hello from Van + Spring Boot!");
        return "index";
    }
}
```

视图名 `"index"` 会自动解析为 `themes/default/pages/index.van`。

## 编程式 API

在 REST API 或非 MVC 场景中直接使用 `VanEngine`：

```java
@RestController
@RequiredArgsConstructor
public class ApiController {

    private final VanEngine engine;

    @GetMapping("/api/render")
    public String render() throws IOException {
        String template = """
                <template>
                  <div>
                    <h1>{{ title }}</h1>
                    <p>{{ message }}</p>
                  </div>
                </template>
                """;

        return engine.compileLiteral(template,
                Map.of("title", "Van API", "message", "Compiled via VanEngine"));
    }
}
```

### VanEngine API

| 方法 | 说明 |
|---|---|
| `getTemplate(path)` | 编译 `.van` 文件，返回可复用的 `VanTemplate` |
| `getTemplate(path, files)` | 从内存文件映射编译（classpath 资源） |
| `getLiteralTemplate(content)` | 编译内联模板字符串 |
| `compile(path, model)` | 编译 + 渲染一步到位 |
| `compileLiteral(content, model)` | 编译内联模板 + 渲染一步到位 |

### VanTemplate API

| 方法 | 说明 |
|---|---|
| `evaluate(model)` | 将 `{{ expr }}` 占位符替换为模型数据 |
| `getHtml()` | 返回原始编译后的 HTML |

`VanTemplate` 不可变且线程安全 — 编译一次，多次渲染。

## 配置

```yaml
van:
  theme-default: default       # 当前主题名称（默认："default"）
  themes-dir: /path/to/themes  # 外部主题目录（可选，优先于 classpath）
```

### 模板解析顺序

1. **外部目录**：`{themes-dir}/{theme-default}/pages/{viewName}.van`
2. **Classpath**：`themes/{theme-default}/pages/{viewName}.van`

## 工作原理

```
.van 文件 → [VanCompiler (WASM)] → 带 {{ expr }} 的 HTML → [VanTemplate.evaluate()] → 最终 HTML
               (昂贵，有缓存)                                  (廉价，正则替换)
```

- **VanCompiler** 管理长驻的 WASM 守护进程，通过 JSON stdin/stdout 编译 `.van` 文件
- **VanTemplate** 持有编译后的 HTML，执行 `{{ expr }}` 插值
- **VanEngine** 是主要门面，串联编译与渲染

编译器二进制文件首次使用时从 [GitHub Releases](https://github.com/vanengine/van/releases) 自动下载并缓存至 `~/.van/bin/`。

## 环境要求

- Java 25+
- Spring Boot 4.0+

## 相关项目

- [**Van**](https://github.com/vanengine/van) — 核心模板引擎（Rust / WASM）
- [**van-java-core**](https://github.com/van-java/van-java-core) — 纯 Java 编译引擎（本 starter 的底层依赖）

## 许可证

[MIT](../../../LICENSE)
