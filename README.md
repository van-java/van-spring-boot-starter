# van-spring-boot-starter

<p>
  <a href="https://github.com/vanengine/van"><img src="https://img.shields.io/badge/van-template%20engine-steelblue" alt="Van" /></a>
  <a href="https://central.sonatype.com/artifact/dev.vanengine/van-spring-boot-starter"><img src="https://img.shields.io/maven-central/v/dev.vanengine/van-spring-boot-starter" alt="Maven Central" /></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/license-MIT-blue.svg" alt="License" /></a>
</p>

Spring Boot integration for the [Van](https://github.com/vanengine/van) template engine — write templates with Vue SFC syntax, render server-side HTML with zero Node.js dependency.

## Quick Start

### 1. Add dependency

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

### 2. Create a template

`src/main/resources/themes/default/pages/index.van`:

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

### 3. Return the view

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

The view name `"index"` resolves to `themes/default/pages/index.van` automatically.

## Programmatic API

Use `VanEngine` directly for REST APIs or non-MVC use cases:

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

| Method | Description |
|---|---|
| `getTemplate(path)` | Compile a `.van` file and return a reusable `VanTemplate` |
| `getTemplate(path, files)` | Compile from an in-memory files map (classpath resources) |
| `getLiteralTemplate(content)` | Compile an inline template string |
| `compile(path, model)` | Compile + evaluate in one step |
| `compileLiteral(content, model)` | Compile inline + evaluate in one step |

### VanTemplate API

| Method | Description |
|---|---|
| `evaluate(model)` | Interpolate `{{ expr }}` placeholders with model data |
| `getHtml()` | Return the raw compiled HTML |

`VanTemplate` is immutable and thread-safe — compile once, evaluate many times.

## Configuration

```yaml
van:
  theme-default: default       # Active theme name (default: "default")
  themes-dir: /path/to/themes  # External themes directory (optional, overrides classpath)
```

### Template resolution order

1. **External directory**: `{themes-dir}/{theme-default}/pages/{viewName}.van`
2. **Classpath**: `themes/{theme-default}/pages/{viewName}.van`

## How it works

```
.van file → [VanCompiler (WASM)] → HTML with {{ expr }} → [VanTemplate.evaluate()] → Final HTML
              (expensive, cached)                           (cheap, regex-based)
```

- **VanCompiler** manages a long-lived WASM daemon process, compiling `.van` files via JSON stdin/stdout
- **VanTemplate** holds the compiled HTML and performs `{{ expr }}` interpolation with model data
- **VanEngine** is the main facade, wiring compilation and evaluation together

The compiler binary is auto-downloaded from [GitHub Releases](https://github.com/vanengine/van/releases) on first use and cached at `~/.van/bin/`.

## Requirements

- Java 25+
- Spring Boot 4.0+

## Related

- [**Van**](https://github.com/vanengine/van) — Core template engine (Rust / WASM)
- [**van-java-core**](https://github.com/van-java/van-java-core) — Pure Java compilation engine (used by this starter)

## License

[MIT](LICENSE)
