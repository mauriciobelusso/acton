package dev.acton.router.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

final class ActOnHandlerAdapterCustomizer implements BeanPostProcessor {

    private final ObjectMapper mapper;

    ActOnHandlerAdapterCustomizer(ObjectMapper mapper) { this.mapper = mapper; }

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @Nullable String beanName) throws BeansException {
        if (bean instanceof RequestMappingHandlerAdapter adapter) {
            List<HandlerMethodReturnValueHandler> current = adapter.getReturnValueHandlers();
            if (current == null) return bean;

            List<HandlerMethodReturnValueHandler> updated = new ArrayList<>(current.size() + 1);
            updated.add(new ActOnReturnValueHandler(mapper));
            updated.addAll(current);
            adapter.setReturnValueHandlers(updated);

            System.out.println("[ActOn] Injected ActOnReturnValueHandler with highest precedence");
        }
        return bean;
    }
}
