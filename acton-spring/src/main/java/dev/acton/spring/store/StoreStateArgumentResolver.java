package dev.acton.spring.store;

import dev.acton.core.store.StoreState;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public final class StoreStateArgumentResolver implements HandlerMethodArgumentResolver {

    private final ActOnStoreProvider provider;

    public StoreStateArgumentResolver(ActOnStoreProvider provider) { this.provider = provider; }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return StoreState.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        ResolvableType rt = ResolvableType.forMethodParameter(parameter);
        Class<?> entityType = rt.getGeneric(0).resolve();
        if (entityType == null) throw new IllegalStateException("Cannot resolve generic type for StoreState<T>");
        return provider.get(entityType);
    }
}
