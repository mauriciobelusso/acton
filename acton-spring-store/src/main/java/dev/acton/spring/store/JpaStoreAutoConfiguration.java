package dev.acton.spring.store;

import dev.acton.core.store.StoreFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(name = "jakarta.persistence.EntityManager")
@ConditionalOnProperty(prefix = "acton.store", name = "type", havingValue = "jpa", matchIfMissing = true)
@ConditionalOnMissingBean(StoreFactory.class)
public class JpaStoreAutoConfiguration {

  @Bean
  public StoreFactory jpaStoreFactory(jakarta.persistence.EntityManager em) {
    return new JpaStoreFactory(em);
  }
}
