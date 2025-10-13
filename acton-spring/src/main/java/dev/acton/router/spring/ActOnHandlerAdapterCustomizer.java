package dev.acton.router.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.PriorityOrdered;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

@org.springframework.core.annotation.Order(org.springframework.core.Ordered.HIGHEST_PRECEDENCE)
final class ActOnHandlerAdapterCustomizer
        implements BeanPostProcessor, ApplicationContextAware, PriorityOrdered {

    private ApplicationContext ctx;

    @Override public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ctx = applicationContext;
    }

    @Override public int getOrder() { return HIGHEST_PRECEDENCE; }

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @Nullable String beanName) throws BeansException {
        if (bean instanceof RequestMappingHandlerAdapter adapter) {
            List<HandlerMethodReturnValueHandler> current = adapter.getReturnValueHandlers();
            if (current == null) return bean;

            ObjectProvider<ObjectMapper> provider = ctx.getBeanProvider(ObjectMapper.class);
            ObjectMapper mapper = provider.getIfAvailable(ObjectMapper::new);

            List<HandlerMethodReturnValueHandler> updated = new ArrayList<>(current.size() + 1);
            updated.add(new ActOnReturnValueHandler(mapper));
            updated.addAll(current);
            adapter.setReturnValueHandlers(updated);
        }
        return bean;
    }
}
