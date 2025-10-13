package dev.acton.router.spring;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.acton.router.RouteBinding;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

class ActOnHttpHandler {

    private final ObjectMapper mapper;
    private final Map<String, RouteBinding> pathToBinding = new ConcurrentHashMap<>();

    ActOnHttpHandler(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    void bind(String path, RouteBinding rb) {
        pathToBinding.put(path, rb);
    }

    @ResponseBody
    public ResponseEntity<?> handle(HttpServletRequest req, @RequestBody(required = false) String body) throws Exception {
        String path = req.getRequestURI();
        var rb = pathToBinding.get(path);
        if (rb == null) return ResponseEntity.notFound().build();

        Object arg = resolve(rb, body);
        Object result = rb.method().invoke(rb.actor(), arg);

        if (result == null || rb.returnRawType() == Void.TYPE || rb.returnRawType() == Void.class)
            return ResponseEntity.noContent().build();

        return ResponseEntity.ok(result);
    }

    private Object resolve(RouteBinding rb, String body) throws Exception {
        var p = rb.method().getParameters()[0];
        var pt = p.getParameterizedType();
        if (rb.paramIsCollection()) {
            var elem = (Class<?>) ((java.lang.reflect.ParameterizedType) pt).getActualTypeArguments()[0];
            JavaType lt = mapper.getTypeFactory().constructCollectionType(List.class, elem);
            return mapper.readValue(body == null ? "[]" : body, lt);
        }
        return mapper.readValue(body == null ? "{}" : body, mapper.constructType(pt));
    }
}
