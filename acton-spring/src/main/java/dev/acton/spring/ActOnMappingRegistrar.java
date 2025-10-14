package dev.acton.spring;

import dev.acton.core.annotation.Actor;
import dev.acton.core.annotation.Contract;
import dev.acton.spring.util.HttpUtils;
import dev.acton.spring.util.MethodUtils;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.logging.Logger;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

final class ActOnMappingRegistrar implements SmartInitializingSingleton {

    private static final Logger logger = Logger.getLogger(ActOnMappingRegistrar.class.getName());

    private final ApplicationContext ctx;
    private final RequestMappingHandlerMapping mapping;

    ActOnMappingRegistrar(ApplicationContext ctx, RequestMappingHandlerMapping mapping) {
        this.ctx = ctx;
        this.mapping = mapping;
    }

    @Override
    public void afterSingletonsInstantiated() {
        Map<String, Object> actors = ctx.getBeansWithAnnotation(Actor.class);
        for (Object actor : actors.values()) {
            for (Method m : actor.getClass().getMethods()) {
                var payloadOpt = MethodUtils.getPayload(m);

                if (payloadOpt.isEmpty()) continue;

                var c = MethodUtils.getContract(payloadOpt.get());

                HttpSpec http = HttpSpec.from(c);

                var info = RequestMappingInfo.paths(http.path())
                        .methods(http.method())
                        .produces(http.produces())
                        .consumes(http.consumes())
                        .build();

                mapping.registerMapping(info, actor, m);

               logger.info("[ActOn] Mapped %s %s".formatted(http.method(), http.path()));
            }
        }
    }

    record HttpSpec(RequestMethod method, String path, String[] consumes, String[] produces) {
        static HttpSpec from(Contract c) {
            var http = c.http();
            String name = c.value();
            RequestMethod method = HttpUtils.toSpringMethod(http.method());
            String path = !http.path().isEmpty() ? http.path() : HttpUtils.derivePath(name);
            return new HttpSpec(method, path, http.consumes(), http.produces());
        }
    }
}
