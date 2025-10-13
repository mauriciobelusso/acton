package dev.acton.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.acton.core.annotation.Contract;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import org.springframework.core.MethodParameter;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.util.UriComponentsBuilder;

final class ContractPayloadArgumentResolver implements HandlerMethodArgumentResolver {

    private final ObjectMapper mapper;

    ContractPayloadArgumentResolver(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> t = parameter.getParameterType();
        return t.isRecord() && t.isAnnotationPresent(Contract.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest req = webRequest.getNativeRequest(HttpServletRequest.class);
        if (req == null) return null;

        String method = req.getMethod();
        if ("GET".equalsIgnoreCase(method)) {
            var map = queryParams(req);
            return mapper.convertValue(map, mapper.constructType(parameter.getGenericParameterType()));
        } else {
            try (var is = req.getInputStream()) {
                byte[] bytes = is.readAllBytes();
                if (bytes.length == 0) {
                    return mapper.readValue("{}", mapper.constructType(parameter.getGenericParameterType()));
                }
                return mapper.readValue(bytes, mapper.constructType(parameter.getGenericParameterType()));
            }
        }
    }

    private HashMap<String, Object> queryParams(HttpServletRequest req) {
        MultiValueMap<String,String> params = UriComponentsBuilder
                .fromUriString(req.getRequestURI() + (req.getQueryString()==null ? "" : "?"+req.getQueryString()))
                .build()
                .getQueryParams();
        var map = new HashMap<String, Object>();
        params.forEach((k,v) -> map.put(k, v.isEmpty() ? null : v.getFirst()));
        return map;
    }
}
