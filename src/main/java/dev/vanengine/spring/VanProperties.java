package dev.vanengine.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "van")
public class VanProperties {

    /**
     * Active theme name for template lookup (default: "default").
     */
    private String themeDefault = "default";

    /**
     * External directory containing .van theme files. When set, files here
     * override classpath templates with the same path.
     */
    private String themesDir;

    public String getThemeDefault() {
        return themeDefault;
    }

    public void setThemeDefault(String themeDefault) {
        this.themeDefault = themeDefault;
    }

    public String getThemesDir() {
        return themesDir;
    }

    public void setThemesDir(String themesDir) {
        this.themesDir = themesDir;
    }
}
