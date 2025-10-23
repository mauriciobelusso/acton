package dev.acton.spring.bind;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.acton.core.bind.ActOnBinder;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Role;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@AutoConfiguration
public class ActOnAutoBindingConfiguration {

    @Bean @Role(ROLE_INFRASTRUCTURE)
    public ActOnBinder actOnBinder() {
        return new DefaultActOnBinder();
    }

    @Bean @Role(ROLE_INFRASTRUCTURE)
    public MapperRegistry mapperRegistry(org.springframework.beans.factory.ListableBeanFactory f) {
        return new MapperRegistry(f);
    }

    @Bean @Role(ROLE_INFRASTRUCTURE)
    public AutoBinder autoBinder(org.springframework.context.ApplicationContext ctx,
                                 DefaultActOnBinder binder,
                                 MapperRegistry mappers,
                                 BeanFactory factory) {
        return new AutoBinder(ctx, binder, mappers, factory);
    }

    @Bean
    public BinderMappingRegistrar binderMappingRegistrar(@Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping mapping,
                                                         ObjectMapper mapper,
                                                         DefaultActOnBinder binder) {
        return new BinderMappingRegistrar(mapping, mapper, binder);
    }
}
