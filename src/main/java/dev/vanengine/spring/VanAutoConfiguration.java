package dev.vanengine.spring;

import dev.vanengine.core.VanCompiler;
import dev.vanengine.core.VanRenderer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.ViewResolver;

@AutoConfiguration
@ConditionalOnClass(ViewResolver.class)
@EnableConfigurationProperties(VanProperties.class)
public class VanAutoConfiguration {

    private final VanProperties properties;

    public VanAutoConfiguration(VanProperties properties) {
        this.properties = properties;
    }

    @Bean
    public VanRenderer vanRenderer() {
        return new VanRenderer();
    }

    @Bean(initMethod = "init", destroyMethod = "close")
    public VanCompiler vanCompiler() {
        return new VanCompiler();
    }

    @Bean
    public VanViewResolver vanViewResolver(VanRenderer renderer, VanCompiler compiler) {
        return new VanViewResolver(renderer, compiler, properties);
    }
}
