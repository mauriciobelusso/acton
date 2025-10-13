package dev.acton.router.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@AutoConfiguration
public class ActOnSpringAutoConfiguration {

    @Bean
    WebMvcConfigurer actOnMvcConfigurer(ObjectMapper mapper, ApplicationContext ctx) {
        return new WebMvcConfigurer() {
            @Override
            public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
                resolvers.add(new ContractPayloadArgumentResolver(mapper));
                resolvers.add(new StoreStateArgumentResolver(ctx));
            }
        };
    }

    @Bean
    ActOnMappingRegistrar actOnMappingRegistrar(ApplicationContext ctx,
                                                RequestMappingHandlerMapping mapping) {
        return new ActOnMappingRegistrar(ctx, mapping);
    }
}
