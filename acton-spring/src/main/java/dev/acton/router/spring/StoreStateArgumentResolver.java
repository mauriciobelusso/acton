package dev.acton.router.spring;

import dev.acton.core.store.StoreState;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

final class StoreStateArgumentResolver implements HandlerMethodArgumentResolver {

    private final ApplicationContext ctx;

    StoreStateArgumentResolver(ApplicationContext ctx) { this.ctx = ctx; }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return StoreState.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        return ctx.getBean(StoreState.class);
    }
}
