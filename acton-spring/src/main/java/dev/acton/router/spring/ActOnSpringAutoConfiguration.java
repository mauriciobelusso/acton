package dev.acton.router.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.acton.core.router.ParamResolver;
import dev.acton.core.router.resolver.BeanParamResolver;
import dev.acton.core.router.resolver.SimpleWebParamResolver;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@AutoConfiguration
public class ActOnSpringAutoConfiguration {

    @Bean
    ObjectMapper actonObjectMapper() { return new ObjectMapper(); }

    @Bean
    ParamResolver beanParamResolver() { return new BeanParamResolver(); }

    @Bean
    ParamResolver servletParamResolver() { return new ServletParamResolver(); }

    @Bean
    ParamResolver simpleWebParamResolver() { return new SimpleWebParamResolver(); }

    @Bean
    ActOnMappingRegistrar actOnMappingRegistrar(ApplicationContext ctx,
                                                RequestMappingHandlerMapping mapping,
                                                ObjectMapper mapper,
                                                List<ParamResolver> resolvers) {
        var ordered = new ArrayList<ParamResolver>();
        resolvers.stream().filter(r -> r instanceof ServletParamResolver).forEach(ordered::add);
        resolvers.stream().filter(r -> r instanceof SimpleWebParamResolver).forEach(ordered::add);
        resolvers.stream().filter(r -> r instanceof BeanParamResolver).forEach(ordered::add);

        resolvers.stream().filter(r -> !ordered.contains(r)).forEach(ordered::add);

        return new ActOnMappingRegistrar(ctx, mapping, mapper, ordered);
    }
}
