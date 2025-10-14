package dev.acton.spring.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.util.List;
import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Role;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AutoConfiguration
@Import(ActOnStoreInjectionConfig.class)
public class ActOnStoreAutoConfiguration {

    @Bean @Role(ROLE_INFRASTRUCTURE)
    ActOnStoreProvider actOnStoreProvider(
            ApplicationContext ctx,
            org.springframework.beans.factory.ObjectProvider<JpaStoreFactory> jpa,
            org.springframework.beans.factory.ObjectProvider<InMemoryStoreFactory> mem) {
        return new ActOnStoreProvider(ctx, jpa, mem);
    }

    @Bean @Role(ROLE_INFRASTRUCTURE)
    WebMvcConfigurer actOnStoreMvcConfigurer(ActOnStoreProvider provider, ObjectMapper mapper) {
        return new WebMvcConfigurer() {
            @Override public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
                resolvers.add(new StoreStateArgumentResolver(provider));
            }
        };
    }

    @Bean @ConditionalOnClass(EntityManager.class) @ConditionalOnMissingBean
    JpaStoreFactory jpaStoreFactory(EntityManager em) { return new JpaStoreFactory(em); }

    @Bean @ConditionalOnMissingBean
    InMemoryStoreFactory inMemoryStoreFactory() { return new InMemoryStoreFactory(); }
}
