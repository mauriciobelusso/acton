package dev.acton.router.spring;

import dev.acton.core.router.ParamResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public final class ServletParamResolver implements ParamResolver {
    @Override
    public boolean supports(Method method, int paramIndex, Parameter parameter) {
        Class<?> t = parameter.getType();
        return t == HttpServletRequest.class || t == HttpServletResponse.class;
    }

    @Override
    public Object resolve(Method method, int paramIndex, Parameter parameter, ResolutionContext ctx) {
        SpringResolutionContext sc = (SpringResolutionContext) ctx;
        if (parameter.getType() == HttpServletRequest.class) return sc.request();
        if (parameter.getType() == HttpServletResponse.class) return sc.response();
        return null;
    }
}
