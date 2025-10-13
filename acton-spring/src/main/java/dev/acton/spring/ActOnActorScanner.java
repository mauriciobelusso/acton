package dev.acton.spring;

import dev.acton.core.annotation.Actor;
import java.util.List;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.type.filter.AnnotationTypeFilter;

/**
 * Registers all @Actor classes as Spring beans, using Boot's base packages.
 * No @Component required on the actor class.
 */
final class ActOnActorScanner
        implements BeanDefinitionRegistryPostProcessor, BeanFactoryAware, PriorityOrdered {

    private BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override public int getOrder() { return PriorityOrdered.HIGHEST_PRECEDENCE; }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        // Descobre os base packages definidos pelo Boot (@SpringBootApplication / @ComponentScan)
        List<String> bases = AutoConfigurationPackages.has(this.beanFactory)
                ? AutoConfigurationPackages.get(this.beanFactory)
                : List.of();
        if (bases.isEmpty()) {
            // Sem base packages (caso extremo). Não faz nada.
            System.out.println("[ActOn] No base packages discovered. Skipping @Actor scan.");
            return;
        }

        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Actor.class));
        var nameGen = new AnnotationBeanNameGenerator();

        for (String base : bases) {
            for (BeanDefinition bd : scanner.findCandidateComponents(base)) {
                // Gera um bean name consistente (mesma regra do Spring)
                String beanName = nameGen.generateBeanName(bd, registry);
                if (registry.containsBeanDefinition(beanName)) {
                    // Já existe — pula (talvez o dev já marcou com @Component, etc.)
                    continue;
                }
                // Registra o bean
                registry.registerBeanDefinition(beanName, bd);
                System.out.printf("[ActOn] Registered @Actor bean: %s as '%s'%n",
                        bd.getBeanClassName(), beanName);
            }
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) { /* no-op */ }
}
