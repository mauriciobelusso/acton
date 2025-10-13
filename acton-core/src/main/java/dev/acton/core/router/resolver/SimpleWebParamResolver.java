package dev.acton.core.router.resolver;

import dev.acton.core.router.ParamResolver;
import dev.acton.core.router.annotation.Header;
import dev.acton.core.router.annotation.PathVar;
import dev.acton.core.router.annotation.Query;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public final class SimpleWebParamResolver implements ParamResolver {
    @Override
    public boolean supports(Method method, int paramIndex, Parameter parameter) {
        return parameter.isAnnotationPresent(PathVar.class)
            || parameter.isAnnotationPresent(Query.class)
            || parameter.isAnnotationPresent(Header.class);
    }

    @Override
    public Object resolve(Method method, int paramIndex, Parameter parameter, ResolutionContext ctx) {
        if (parameter.isAnnotationPresent(PathVar.class)) {
            return ctx.pathVariable(parameter.getAnnotation(PathVar.class).value());
        }
        if (parameter.isAnnotationPresent(Query.class)) {
            return ctx.queryParam(parameter.getAnnotation(Query.class).value());
        }
        if (parameter.isAnnotationPresent(Header.class)) {
            return ctx.header(parameter.getAnnotation(Header.class).value());
        }
        return null;
    }
}
