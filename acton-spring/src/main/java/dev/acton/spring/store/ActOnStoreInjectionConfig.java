package dev.acton.spring.store;

import dev.acton.core.store.StoreState;
import java.lang.reflect.Field;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;

@Configuration
public class ActOnStoreInjectionConfig {

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @SuppressWarnings("rawtypes")
    public StoreState storeState(InjectionPoint ip, ActOnStoreProvider provider) {
        Class<?> entityType = resolveGenericArgument(ip);
        if (entityType == null) {
            throw new IllegalStateException("Cannot resolve generic for StoreState<T> at injection point: " + ip);
        }
        return provider.get(entityType);
    }

    private static Class<?> resolveGenericArgument(InjectionPoint ip) {
        MethodParameter mp = ip.getMethodParameter();
        if (mp != null) {
            ResolvableType rt = ResolvableType.forMethodParameter(mp);
            return rt.getGeneric(0).resolve();
        }
        Field field = ip.getField();
        if (field != null) {
            ResolvableType rt = ResolvableType.forField(field);
            return rt.getGeneric(0).resolve();
        }
        var annotated = ip.getAnnotatedElement();
        if (annotated instanceof Field f) {
            ResolvableType rt = ResolvableType.forField(f);
            return rt.getGeneric(0).resolve();
        }
        return null;
    }
}
