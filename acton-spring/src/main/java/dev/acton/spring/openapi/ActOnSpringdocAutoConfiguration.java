package dev.acton.spring.openapi;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(OpenApiCustomizer.class)
public class ActOnSpringdocAutoConfiguration {

    @Bean
    public OpenApiCustomizer actOnCustomizer(
            org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping mapping) {
        return new ActOnOpenApiCustomizer(mapping);
    }
}
