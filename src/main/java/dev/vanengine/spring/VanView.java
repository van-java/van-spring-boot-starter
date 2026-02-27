package dev.vanengine.spring;

import dev.vanengine.core.VanEngine;
import dev.vanengine.core.VanTemplate;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.View;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * A Spring View backed by VanEngine
 * Holds a template path (+ optional files map) and resolves to a VanTemplate at render time.
 */
public class VanView implements View {

    private final VanEngine engine;
    private final String templatePath;
    private final Map<String, String> files; // null = filesystem mode

    public VanView(VanEngine engine, String templatePath, Map<String, String> files) {
        this.engine = engine;
        this.templatePath = templatePath;
        this.files = files;
    }

    @Override
    public String getContentType() {
        return MediaType.TEXT_HTML_VALUE;
    }

    @Override
    public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        VanTemplate template = (files != null)
                ? engine.getTemplate(templatePath, files)
                : engine.getTemplate(templatePath);
        String html = template.evaluate(model != null ? model : Map.of());
        response.setContentType(MediaType.TEXT_HTML_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(html);
    }
}
