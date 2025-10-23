package dev.acton.spring.store;

import dev.acton.core.store.StoreFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = JpaStoreAutoConfiguration.class)
@ConditionalOnMissingBean(StoreFactory.class)
public class InMemoryStoreAutoConfiguration {

  @Bean
  public StoreFactory inMemoryStoreFactory() {
    return new InMemoryStoreFactory();
  }
}
