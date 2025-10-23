package dev.acton.spring.bind;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.acton.core.annotation.Contract;
import dev.acton.core.bind.ActOnBinder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;

public final class BinderSpringHandler<Q> {
    private final Class<Q> contractType;
    private final ActOnBinder.Handler<Q> handler;
    private final ObjectMapper mapper;
    private final Type returnType;

    public BinderSpringHandler(Class<Q> contractType,
                        ActOnBinder.Handler<Q> handler,
                        ObjectMapper mapper) {
        this(contractType, handler, mapper, null);
    }

    public BinderSpringHandler(Class<Q> contractType,
                        ActOnBinder.Handler<Q> handler,
                        ObjectMapper mapper,
                        Type returnType) {
        this.contractType = contractType;
        this.handler = handler;
        this.mapper = mapper;
        this.returnType = returnType;
    }

    public Class<?> contractType() { return contractType; }
    public Type    returnType()    { return returnType; }

    public void handle(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Q payload = buildPayload(req);
        Object result = handler.handle(payload);

        Contract c = contractType.getAnnotation(Contract.class);
        String[] produces = c.http().produces();
        String ct = (produces.length == 0) ? "application/json" : produces[0];

        resp.setStatus(HttpStatus.OK.value());
        resp.setContentType(ct);

        try (OutputStream os = resp.getOutputStream()) {
            if ("text/plain".equals(ct) && result instanceof CharSequence cs) {
                os.write(cs.toString().getBytes(StandardCharsets.UTF_8));
            } else {
                mapper.writeValue(os, result);
            }
        }
    }

    private Q buildPayload(HttpServletRequest req) throws Exception {
        Contract c = contractType.getAnnotation(Contract.class);
        boolean hasBody = switch (c.http().method()) {
            case POST, PUT, PATCH -> true; default -> false;
        };
        if (hasBody) try (InputStream is = req.getInputStream()) {
            return mapper.readValue(is, contractType);
        }
        return mapper.convertValue(req.getParameterMap(), contractType);
    }
}
