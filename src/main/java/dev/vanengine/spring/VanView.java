package dev.vanengine.spring;

import dev.vanengine.core.VanRenderer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.View;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Renders compiled HTML by performing {{ expr }} interpolation with Spring Model data.
 */
public class VanView implements View {

    private final VanRenderer renderer;
    private final String html;

    public VanView(VanRenderer renderer, String html) {
        this.renderer = renderer;
        this.html = html;
    }

    @Override
    public String getContentType() {
        return MediaType.TEXT_HTML_VALUE;
    }

    @Override
    public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String finalHtml = renderer.render(html, model != null ? model : Map.of());
        response.setContentType(MediaType.TEXT_HTML_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(finalHtml);
    }
}
