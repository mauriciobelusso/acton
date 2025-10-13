package dev.acton.core.router.resolver;

import dev.acton.core.router.ParamResolver;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public final class BeanParamResolver implements ParamResolver {
    @Override
    public boolean supports(Method method, int paramIndex, Parameter parameter) {
        // tenta injetar beans Spring para tipos que não parecem ser o payload record
        Class<?> t = parameter.getType();
        return !t.isPrimitive() && !t.isRecord() && !t.getName().startsWith("java.lang");
    }

    @Override
    public Object resolve(Method method, int paramIndex, Parameter parameter, ResolutionContext ctx) {
        try { return ctx.getBean(parameter.getType()); } catch (Exception e) { return null; }
    }
}
