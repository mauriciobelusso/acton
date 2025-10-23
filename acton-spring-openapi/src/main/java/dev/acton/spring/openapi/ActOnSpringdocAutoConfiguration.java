package dev.acton.spring.openapi;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@AutoConfiguration
public class ActOnSpringdocAutoConfiguration {

    @Bean
    public OpenApiCustomizer actOnCustomizer(
            @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping mapping) {
        return new ActOnOpenApiCustomizer(mapping);
    }
}
