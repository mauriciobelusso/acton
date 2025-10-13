package dev.acton.router.spring;

import dev.acton.core.router.ParamResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.springframework.context.ApplicationContext;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

final class SpringResolutionContext implements ParamResolver.ResolutionContext {
    private final ApplicationContext ctx;
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final String body;
    private final Map<String, String> pathVars;
    private final MultiValueMap<String, String> query;

    SpringResolutionContext(ApplicationContext ctx,
                            HttpServletRequest request,
                            HttpServletResponse response,
                            String body,
                            Map<String, String> pathVars) {
        this.ctx = ctx;
        this.request = request;
        this.response = response;
        this.body = body;
        this.pathVars = pathVars;
        this.query = UriComponentsBuilder.fromUriString(request.getRequestURI() +
                (request.getQueryString() == null ? "" : "?" + request.getQueryString()))
                .build()
                .getQueryParams();
    }

    @Override public <T> T getBean(Class<T> type) { return ctx.getBean(type); }
    @Override public String body() { return body; }
    @Override public String pathVariable(String name) { return pathVars.get(name); }
    @Override public String queryParam(String name) { return query.getFirst(name); }
    @Override public String header(String name) { return request.getHeader(name); }
    @Override public ClassLoader classLoader() { return ctx.getClassLoader(); }

    HttpServletRequest request() { return request; }
    HttpServletResponse response() { return response; }
}
