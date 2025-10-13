package dev.acton.router.spring;

import dev.acton.router.RouteBinding;
import dev.acton.router.Router;
import java.lang.reflect.Method;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

final class ActOnRouteRegistrar {

    static void register(RequestMappingHandlerMapping mapping, Router router, ActOnHttpHandler handler) throws Exception {
        Method handle = ActOnHttpHandler.class.getMethod(
                "handle",
                jakarta.servlet.http.HttpServletRequest.class,
                String.class
        );

        for (RouteBinding rb : router.routes()) {
            var http = HttpSpec.from(rb);

            var info = RequestMappingInfo
                    .paths(http.path())
                    .methods(http.method())
                    .produces(MediaType.APPLICATION_JSON_VALUE)
                    .build();

            mapping.registerMapping(info, handler, handle);
            handler.bind(http.path(), rb);
        }
    }
}
