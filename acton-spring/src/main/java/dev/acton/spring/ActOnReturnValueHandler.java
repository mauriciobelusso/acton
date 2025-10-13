package dev.acton.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.acton.core.annotation.Contract;
import jakarta.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.AsyncHandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

final class ActOnReturnValueHandler implements AsyncHandlerMethodReturnValueHandler {

    private final ObjectMapper mapper;

    ActOnReturnValueHandler(ObjectMapper mapper) { this.mapper = mapper; }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        Parameter[] params = returnType.getMethod().getParameters();
        if (params.length == 0) return false;
        Class<?> first = params[0].getType();
        return first.isRecord() && first.isAnnotationPresent(Contract.class);
    }

    @Override
    public boolean isAsyncReturnValue(@Nullable Object returnValue, MethodParameter returnType) {
        return false;
    }

    @Override
    public void handleReturnValue(@Nullable Object returnValue,
                                  MethodParameter returnType,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest) throws Exception {
        mavContainer.setRequestHandled(true);

        HttpServletResponse resp = webRequest.getNativeResponse(HttpServletResponse.class);
        if (resp == null) return;

        if (returnValue instanceof ResponseEntity<?> re) {
            resp.setStatus(re.getStatusCode().value());
            MediaType ct = re.getHeaders().getContentType();
            String contentType = (ct != null ? ct.toString() : resolveProduces(returnType));
            if (contentType != null) resp.setContentType(contentType);

            Object body = re.getBody();
            if (body == null) return;

            writeBody(resp, body, contentType);
            return;
        }

        if (returnValue == null || returnType.getParameterType() == Void.TYPE) {
            resp.setStatus(HttpStatus.NO_CONTENT.value());
            return;
        }

        if (returnValue instanceof Optional<?> opt) {
            if (opt.isEmpty()) {
                resp.setStatus(HttpStatus.NO_CONTENT.value());
                return;
            }
            returnValue = opt.get();
        }

        String contentType = resolveProduces(returnType);
        if (contentType != null) resp.setContentType(contentType);
        resp.setStatus(HttpStatus.OK.value());

        writeBody(resp, returnValue, contentType);
    }

    private String resolveProduces(MethodParameter returnType) {
        Parameter[] params = returnType.getMethod().getParameters();
        if (params.length == 0) return MediaType.APPLICATION_JSON_VALUE;

        Class<?> first = params[0].getType();
        Contract c = first.getAnnotation(Contract.class);
        if (c == null) return MediaType.APPLICATION_JSON_VALUE;

        String[] produces = c.http().produces();
        if (produces == null || produces.length == 0) return MediaType.APPLICATION_JSON_VALUE;
        return produces[0];
    }

    private void writeBody(HttpServletResponse resp, Object body, @Nullable String contentType) throws Exception {
        String ct = (contentType == null) ? MediaType.APPLICATION_JSON_VALUE : contentType;

        try (OutputStream os = resp.getOutputStream()) {
            switch (ct) {
                case MediaType.APPLICATION_JSON_VALUE -> {
                    mapper.writeValue(os, body);
                    return;
                }
                case MediaType.TEXT_PLAIN_VALUE -> {
                    String s = (body instanceof CharSequence cs) ? cs.toString() : String.valueOf(body);
                    os.write(s.getBytes(StandardCharsets.UTF_8));
                    return;
                }
                case MediaType.APPLICATION_OCTET_STREAM_VALUE -> {
                    if (body instanceof byte[] bytes) {
                        os.write(bytes);
                        return;
                    }
                    mapper.writeValue(os, body);
                    return;
                }
            }
            if (body instanceof CharSequence cs) {
                os.write(cs.toString().getBytes(StandardCharsets.UTF_8));
            } else {
                mapper.writeValue(os, body);
            }
        }
    }
}
