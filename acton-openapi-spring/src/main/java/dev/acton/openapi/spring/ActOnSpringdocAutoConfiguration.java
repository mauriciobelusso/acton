package dev.acton.openapi.spring;

import dev.acton.openapi.OpenApiGenerator;
import dev.acton.router.Router;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration that contributes a Springdoc OpenApiCustomizer
 * to merge ActOn routes into the application's OpenAPI model.
 * This activates only if Springdoc is present on the classpath.
 */
@AutoConfiguration
@ConditionalOnClass(OpenApiCustomizer.class)
public class ActOnSpringdocAutoConfiguration {

    @Bean
    OpenApiGenerator actonOpenApiGenerator() {
        return new OpenApiGenerator();
    }

    @Bean
    OpenApiCustomizer actonOpenApiCustomizer(Router router, OpenApiGenerator generator) {
        return new ActOnSpringdocCustomizer(router, generator);
    }
}
