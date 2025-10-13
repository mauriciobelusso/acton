package dev.acton.openapi.spring;

import dev.acton.core.actor.Actor;
import io.swagger.v3.oas.models.OpenAPI;
import java.util.Collection;
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
            Collection<Actor> actors = ctx.getBeansOfType(Actor.class).values();
            var actonDoc = generator.generate(actors);
            ActOnSpringdocMerger.mergeInto(springdocModel, actonDoc);
        };
    }
}
