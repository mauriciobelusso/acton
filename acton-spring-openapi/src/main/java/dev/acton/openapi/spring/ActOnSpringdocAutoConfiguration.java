package dev.acton.openapi.spring;

import dev.acton.core.annotation.Actor;
import io.swagger.v3.oas.models.OpenAPI;
import java.util.Map;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration that contributes a Springdoc OpenApiCustomizer
 * to merge ActOn routes into the application's OpenAPI model.
 * Activates ONLY if Springdoc is on the classpath.
 */
@AutoConfiguration
@ConditionalOnClass(OpenApiCustomizer.class)
public class ActOnSpringdocAutoConfiguration {

    @Bean
    OpenApiGenerator actonOpenApiGenerator() {
        return new OpenApiGenerator();
    }

    @Bean
    OpenApiCustomizer actonOpenApiCustomizer(ApplicationContext ctx, OpenApiGenerator generator) {
        return (OpenAPI springdocModel) -> {
            Map<String, Object> actors = ctx.getBeansWithAnnotation(Actor.class);
            var actonDoc = generator.generate(actors.values());
            ActOnSpringdocMerger.mergeInto(springdocModel, actonDoc);
        };
    }
}
