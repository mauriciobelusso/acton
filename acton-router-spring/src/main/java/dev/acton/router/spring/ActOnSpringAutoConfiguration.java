package dev.acton.router.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.acton.core.actor.Actor;
import dev.acton.router.Router;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@AutoConfiguration
public class ActOnSpringAutoConfiguration {

    @Bean
    Router actonRouter(ApplicationContext ctx) {
        var r = new Router();
        ctx.getBeansOfType(Actor.class).values().forEach(r::registerActor);
        return r;
    }

    @Bean
    ActOnHttpHandler actOnHttpHandler(ObjectMapper mapper) {
        return new ActOnHttpHandler(mapper);
    }

    @Bean
    SmartInitializingSingleton actonRouteRegistrar(
            RequestMappingHandlerMapping mapping,
            ActOnHttpHandler handler,
            Router router) {
        return () -> {
            try {
                ActOnRouteRegistrar.register(mapping, router, handler);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to register ActOn routes", e);
            }
        };
    }
}
