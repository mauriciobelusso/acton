package dev.acton.router.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.acton.core.actor.Actor;
import dev.acton.core.annotation.Contract;
import dev.acton.core.router.ParamResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

final class ActOnMappingRegistrar implements SmartInitializingSingleton {

    private static final Logger log = Logger.getLogger(ActOnMappingRegistrar.class.getName());

    private final ApplicationContext ctx;
    private final RequestMappingHandlerMapping mapping;
    private final ObjectMapper mapper;
    private final List<ParamResolver> resolvers;

    ActOnMappingRegistrar(ApplicationContext ctx,
                          RequestMappingHandlerMapping mapping,
                          ObjectMapper mapper,
                          List<ParamResolver> resolvers) {
        this.ctx = ctx;
        this.mapping = mapping;
        this.mapper = mapper;
        this.resolvers = resolvers;
    }

    @Override
    public void afterSingletonsInstantiated() {
        Map<String, Actor> actors = ctx.getBeansOfType(Actor.class);
        for (Actor actor : actors.values()) {
            for (Method m : actor.getClass().getMethods()) {
                if (!m.getName().equals("on") || m.getParameterCount() == 0) continue;

                Class<?> payload = m.getParameters()[0].getType();
                Contract c = payload.getAnnotation(Contract.class);
                if (c == null) continue;

                HttpSpec http = HttpSpec.from(c);

                RequestMappingInfo info = RequestMappingInfo
                        .paths(http.path())
                        .methods(http.method())
                        .produces(http.produces())
                        .consumes(http.consumes())
                        .build();

                ActOnSpringHandler handler = new ActOnSpringHandler(actor, m, resolvers, mapper, ctx);
                Method handle;
                try {
                    handle = ActOnSpringHandler.class.getMethod("handle", HttpServletRequest.class, HttpServletResponse.class);
                } catch (NoSuchMethodException e) {
                    throw new IllegalStateException(e);
                }
                mapping.registerMapping(info, handler, handle);

                log.info("[ActOn] Mapped %s %s%s%s".formatted(
                        http.method(), http.path(),
                        http.produces().length>0 ? " produces="+Arrays.toString(http.produces()) : "",
                        http.consumes().length>0 ? " consumes="+Arrays.toString(http.consumes()) : ""
                ));
            }
        }
    }

    record HttpSpec(RequestMethod method, String path, String[] consumes, String[] produces) {
        static HttpSpec from(Contract c) {
            var http = c.http();
            String name = c.value();
            RequestMethod method = toSpring(http.method());
            String path = !http.path().isEmpty() ? http.path() : derivePath(name);
            String[] consumes = http.consumes();
            String[] produces = http.produces();
            return new HttpSpec(method, path, consumes, produces);
        }

        private static RequestMethod toSpring(Contract.Http.Method m) {
            return switch (m) {
                case GET -> RequestMethod.GET;
                case POST -> RequestMethod.POST;
                case PUT -> RequestMethod.PUT;
                case DELETE -> RequestMethod.DELETE;
                case PATCH -> RequestMethod.PATCH;
            };
        }

        private static String derivePath(String name) {
            String[] p = name.split("\\.");
            String res = p[0];
            String op  = p.length > 1 ? p[1] : "post";
            return switch (op) {
                case "list", "get", "find", "create", "add" -> "/" + res;
                case "update", "delete", "remove"           -> "/" + res + "/{id}";
                default                                     -> name;
            };
        }
    }
}
