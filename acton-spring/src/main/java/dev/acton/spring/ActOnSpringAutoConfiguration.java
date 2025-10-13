package dev.acton.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Role;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@AutoConfiguration
public class ActOnSpringAutoConfiguration {

    @Bean
    @Role(ROLE_INFRASTRUCTURE)
    static ActOnActorScanner actOnActorScanner() {
        return new ActOnActorScanner();
    }

    @Bean
    WebMvcConfigurer actOnWebMvcConfigurer(ObjectMapper mapper) {
        return new WebMvcConfigurer() {
            @Override
            public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
                resolvers.add(new ContractPayloadArgumentResolver(mapper));
            }
        };
    }

    @Bean
    @Role(ROLE_INFRASTRUCTURE)
    static ActOnHandlerAdapterCustomizer actOnHandlerAdapterCustomizer() {
        return new ActOnHandlerAdapterCustomizer();
    }

    @Bean
    ActOnMappingRegistrar actOnMappingRegistrar(
            org.springframework.context.ApplicationContext ctx,
            RequestMappingHandlerMapping mapping) {
        return new ActOnMappingRegistrar(ctx, mapping);
    }
}
