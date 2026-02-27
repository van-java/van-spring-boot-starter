package dev.vanengine.spring;

import dev.vanengine.core.VanCompiler;
import dev.vanengine.core.VanEngine;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.ViewResolver;

import java.nio.file.Path;

@AutoConfiguration
@ConditionalOnClass(ViewResolver.class)
@EnableConfigurationProperties(VanProperties.class)
public class VanAutoConfiguration {

    private final VanProperties properties;

    public VanAutoConfiguration(VanProperties properties) {
        this.properties = properties;
    }

    @Bean(initMethod = "init", destroyMethod = "close")
    public VanCompiler vanCompiler() {
        return new VanCompiler();
    }

    @Bean
    public VanEngine vanEngine(VanCompiler compiler) {
        VanEngine engine = new VanEngine(compiler);
        if (properties.getThemesDir() != null) {
            engine.setBasePath(Path.of(properties.getThemesDir(), properties.getThemeDefault()));
        }
        return engine;
    }

    @Bean
    public VanViewResolver vanViewResolver(VanEngine engine) {
        return new VanViewResolver(engine, properties);
    }
}
