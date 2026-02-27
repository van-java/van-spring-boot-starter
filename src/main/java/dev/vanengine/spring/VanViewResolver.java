package dev.vanengine.spring;

import dev.vanengine.core.VanCompiler;
import dev.vanengine.core.VanRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

/**
 * Resolves view names to .van templates, compiles them at runtime, and
 * performs {{ expr }} interpolation with Spring Model data.
 *
 * <p>Lookup order:
 * <ol>
 *   <li>External directory: {@code {themesDir}/{themeDefault}/pages/{viewName}.van}</li>
 *   <li>Classpath: {@code themes/{themeDefault}/pages/{viewName}.van}</li>
 * </ol>
 */
public class VanViewResolver implements ViewResolver, Ordered {

    private static final Logger log = LoggerFactory.getLogger(VanViewResolver.class);

    private final VanRenderer renderer;
    private final VanCompiler compiler;
    private final VanProperties properties;

    public VanViewResolver(VanRenderer renderer, VanCompiler compiler, VanProperties properties) {
        this.renderer = renderer;
        this.compiler = compiler;
        this.properties = properties;
    }

    @Override
    public View resolveViewName(String viewName, Locale locale) {
        String themeName = properties.getThemeDefault();

        // 1. External themes directory (takes priority — editable files)
        if (properties.getThemesDir() != null) {
            Path themeDir = Path.of(properties.getThemesDir(), themeName);
            Path vanFile = findVanFile(themeDir, viewName);
            if (vanFile != null) {
                return compileFromFile(vanFile, themeDir);
            }
        }

        // 2. Classpath (JAR-bundled themes)
        String classpathBase = "themes/" + themeName;
        String classpathPage = classpathBase + "/pages/" + viewName + ".van";
        ClassPathResource resource = new ClassPathResource(classpathPage);
        if (resource.exists()) {
            return compileFromClasspath(classpathPage, classpathBase);
        }

        return null;
    }

    private Path findVanFile(Path themeDir, String viewName) {
        // pages/{viewName}.van
        Path page = themeDir.resolve("pages").resolve(viewName + ".van");
        if (Files.isRegularFile(page)) return page;
        return null;
    }

    private View compileFromFile(Path vanFile, Path basePath) {
        try {
            VanCompiler.CompiledResult result = compiler.compile(vanFile, basePath);
            return new VanView(renderer, result.html());
        } catch (IOException e) {
            log.error("Failed to compile {}", vanFile, e);
            return null;
        }
    }

    private View compileFromClasspath(String resourcePath, String basePath) {
        try {
            // Read the entry file from classpath
            ClassPathResource resource = new ClassPathResource(resourcePath);
            String content = resource.getContentAsString(StandardCharsets.UTF_8);

            String entryPath = resourcePath.substring(basePath.length() + 1); // strip "themes/{theme}/"
            Map<String, String> files = new java.util.HashMap<>();
            files.put(entryPath, content);

            // Collect imported .van files from classpath
            collectClasspathImports(content, resourcePath, basePath, files);

            VanCompiler.CompiledResult result = compiler.compile(entryPath, files);
            return new VanView(renderer, result.html());
        } catch (IOException e) {
            log.error("Failed to compile classpath resource {}", resourcePath, e);
            return null;
        }
    }

    private void collectClasspathImports(String content, String currentPath,
                                         String basePath, Map<String, String> files) throws IOException {
        for (String importPath : VanCompiler.parseImportPaths(content)) {
            // Resolve relative to current file's directory
            String currentDir = currentPath.substring(0, currentPath.lastIndexOf('/'));
            String resolvedPath = resolvePath(currentDir, importPath);
            String relativeKey = resolvedPath.substring(basePath.length() + 1);

            if (files.containsKey(relativeKey)) continue;

            ClassPathResource importResource = new ClassPathResource(resolvedPath);
            if (importResource.exists()) {
                String importContent = importResource.getContentAsString(StandardCharsets.UTF_8);
                files.put(relativeKey, importContent);
                collectClasspathImports(importContent, resolvedPath, basePath, files);
            }
        }
    }

    private String resolvePath(String currentDir, String relativePath) {
        // Simple relative path resolution for ./ and ../
        if (relativePath.startsWith("./")) {
            return currentDir + "/" + relativePath.substring(2);
        }
        if (relativePath.startsWith("../")) {
            String parent = currentDir.substring(0, currentDir.lastIndexOf('/'));
            return resolvePath(parent, relativePath.substring(3));
        }
        return currentDir + "/" + relativePath;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 1;
    }
}
