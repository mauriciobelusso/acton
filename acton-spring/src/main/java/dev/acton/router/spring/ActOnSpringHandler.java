package dev.acton.router.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.acton.core.actor.Actor;
import dev.acton.core.router.ParamResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;

final class ActOnSpringHandler {

    private final Actor actor;
    private final Method method;
    private final List<ParamResolver> resolvers;
    private final ObjectMapper mapper;
    private final ApplicationContext ctx;

    ActOnSpringHandler(Actor actor, Method method, List<ParamResolver> resolvers,
                       ObjectMapper mapper, ApplicationContext ctx) {
        this.actor = actor; this.method = method; this.resolvers = resolvers; this.mapper = mapper; this.ctx = ctx;
    }

    @ResponseBody
    public ResponseEntity<?> handle(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String body = readBody(req);
        Map<String, String> pathVars = Collections.emptyMap();
        var rc = new SpringResolutionContext(ctx, req, resp, body, pathVars);

        Object[] args = new Object[method.getParameterCount()];
        Parameter[] params = method.getParameters();

        for (int i = 0; i < params.length; i++) {
            Parameter p = params[i];
            Object resolved = tryResolve(method, i, p, rc);
            if (resolved == null && i == 0) {
                if (!"GET".equalsIgnoreCase(req.getMethod())) {
                    resolved = mapper.readValue(body == null ? "{}" : body, mapper.constructType(p.getParameterizedType()));
                } else {
                    resolved = mapper.readValue("{}", mapper.constructType(p.getParameterizedType()));
                }
            }
            if (resolved == null) {
                throw new IllegalArgumentException("Cannot resolve parameter " + p + " for " + method);
            }
            args[i] = resolved;
        }

        Object result = method.invoke(actor, args);
        if (result == null || method.getReturnType() == Void.TYPE) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(result);
    }

    private Object tryResolve(Method m, int index, Parameter p, ParamResolver.ResolutionContext rc) throws Exception {
        for (ParamResolver r : resolvers) {
            if (r.supports(m, index, p)) {
                Object v = r.resolve(m, index, p, rc);
                if (v != null) return v;
            }
        }
        return null;
    }

    private static String readBody(HttpServletRequest req) throws Exception {
        if ("GET".equalsIgnoreCase(req.getMethod())) return null;
        var in = req.getInputStream();
        if (in == null) return null;
        return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }
}
