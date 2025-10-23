package dev.acton.spring.bind;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.acton.core.annotation.Contract;
import dev.acton.core.util.ContractUtils;
import dev.acton.spring.util.SpringContractUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.core.Ordered;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

public final class BinderMappingRegistrar implements SmartInitializingSingleton, Ordered {

    private static final System.Logger log = System.getLogger(BinderMappingRegistrar.class.getName());

    private final RequestMappingHandlerMapping mapping;
    private final ObjectMapper mapper;
    private final DefaultActOnBinder binder;

    public BinderMappingRegistrar(RequestMappingHandlerMapping mapping,
                                  ObjectMapper mapper,
                                  DefaultActOnBinder binder) {
        this.mapping = mapping; this.mapper = mapper; this.binder = binder;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public void afterSingletonsInstantiated() {
        binder.snapshot().forEach((contractType, handler) -> {
            Contract c = contractType.getAnnotation(Contract.class);
            var http = c.http();
            RequestMethod method = SpringContractUtils.toSpringMethod(http.method());
            String path = ContractUtils.derivePath(c);

            var info = RequestMappingInfo
                    .paths(path).methods(method)
                    .consumes(http.consumes()).produces(http.produces())
                    .build();

            @SuppressWarnings({"rawtypes", "unchecked"})
            var invoker = new BinderSpringHandler(contractType, handler, mapper);
            try {
                Method handle = BinderSpringHandler.class.getMethod("handle", HttpServletRequest.class, HttpServletResponse.class);
                mapping.registerMapping(info, invoker, handle);
                log.log(System.Logger.Level.DEBUG, "[ActOn] Mapped {0} {1}", method, path);
            } catch (NoSuchMethodException e) { throw new IllegalStateException(e); }
        });
    }
}
