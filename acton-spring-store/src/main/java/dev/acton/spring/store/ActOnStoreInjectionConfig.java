package dev.acton.spring.store;

import dev.acton.core.store.Store;
import dev.acton.core.store.StoreFactory;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@AutoConfiguration
public class ActOnStoreInjectionConfig {
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public <T> Store<T> storeState(InjectionPoint ip, StoreFactory storeFactory) {
        var rt = org.springframework.core.ResolvableType.forType(ip.getDeclaredType());
        @SuppressWarnings("unchecked")
        Class<T> entityClass = (Class<T>) rt.as(Store.class).getGeneric(0).resolve();
        return storeFactory.create(entityClass);
    }
}
