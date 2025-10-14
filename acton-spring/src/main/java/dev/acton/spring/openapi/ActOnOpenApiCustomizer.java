package dev.acton.spring.openapi;

import dev.acton.core.annotation.Actor;
import dev.acton.core.annotation.Contract;
import dev.acton.spring.util.HttpUtils;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.util.ClassUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

final class ActOnOpenApiCustomizer implements OpenApiCustomizer {

    private final RequestMappingHandlerMapping mapping;

    ActOnOpenApiCustomizer(RequestMappingHandlerMapping mapping) {
        this.mapping = mapping;
    }

    @Override
    public void customise(OpenAPI openApi) {
        if (openApi.getPaths() == null) openApi.setPaths(new Paths());

        mapping.getHandlerMethods().forEach((info, handler) -> {
            if (!isActOnHandler(handler)) return;

            var contractType = handler.getMethod().getParameters()[0].getType();
            var c = contractType.getAnnotation(Contract.class);
            var http = c.http();

            String path = !http.path().isEmpty() ? http.path() : HttpUtils.derivePath(c.value());

            PathItem.HttpMethod verb = HttpUtils.toOasMethod(http.method());

            PathItem pathItem = openApi.getPaths().computeIfAbsent(path, k -> new PathItem());
            Operation op = new Operation()
                    .summary(c.description().isEmpty() ? c.value() : c.description())
                    .operationId(buildOpId(handler.getMethod(), c.value()));

            boolean hasBody = switch (http.method()) { case POST, PUT, PATCH -> true; default -> false; };
            if (hasBody) {
                Schema<?> schema = toSchema(contractType);
                String consumes = HttpUtils.pickFirstOrDefault(http.consumes(), "application/json");
                op.requestBody(new RequestBody()
                        .content(new Content().addMediaType(consumes, new MediaType().schema(schema))));
            }

            Schema<?> respSchema = toResponseSchema(handler.getMethod().getGenericReturnType());
            String produces = HttpUtils.pickFirstOrDefault(http.produces(), "application/json");
            ApiResponses responses = new ApiResponses()
                    .addApiResponse("200", new ApiResponse()
                            .description("OK")
                            .content(new Content().addMediaType(produces, new MediaType().schema(respSchema))));
            op.setResponses(responses);

            switch (verb) {
                case GET -> pathItem.setGet(op);
                case POST -> pathItem.setPost(op);
                case PUT -> pathItem.setPut(op);
                case DELETE -> pathItem.setDelete(op);
                case PATCH -> pathItem.setPatch(op);
                default -> {}
            }
        });
    }

    private boolean isActOnHandler(HandlerMethod hm) {
        if (!ClassUtils.isPresent(hm.getBeanType().getName(), hm.getBeanType().getClassLoader())) return false;
        if (!hm.getBeanType().isAnnotationPresent(Actor.class)) return false;
        var params = hm.getMethod().getParameters();
        return params.length > 0 && params[0].getType().isAnnotationPresent(Contract.class);
    }

    private String buildOpId(Method m, String contractName) {
        return m.getDeclaringClass().getSimpleName() + "_" + m.getName() + "_" + contractName.replace('.','_');
    }

    private Schema<?> toSchema(Class<?> recordType) {
        Schema<?> s = new Schema<>();
        s.setType("object");
        if (recordType.isRecord()) {
            for (var rc : recordType.getRecordComponents()) {
                s.addProperty(rc.getName(), primitiveOrObject(rc.getGenericType()));
            }
        }
        return s;
    }

    private Schema<?> toResponseSchema(Type type) {
        Type t = unwrap(type);
        if (t instanceof ParameterizedType pt) {
            var raw = (Class<?>) pt.getRawType();
            if (Collection.class.isAssignableFrom(raw)) {
                Schema<?> item = primitiveOrObject(pt.getActualTypeArguments()[0]);
                ArraySchema arr = new ArraySchema();
                arr.setItems(item);
                return arr;
            }
        }
        return primitiveOrObject(t);
    }

    private Type unwrap(Type t) {
        if (t instanceof ParameterizedType pt) {
            Class<?> raw = (Class<?>) pt.getRawType();
            if (raw.getName().equals("org.springframework.http.ResponseEntity")
             || raw.getName().equals("java.util.Optional")) {
                return pt.getActualTypeArguments()[0];
            }
        }
        return t;
    }

    private Schema<?> primitiveOrObject(Type t) {
        if (t instanceof Class<?> c) {
            if (Number.class.isAssignableFrom(c) || c.isPrimitive()) return new Schema<>().type("number");
            if (c == String.class || CharSequence.class.isAssignableFrom(c)) return new Schema<>().type("string");
            if (c == Boolean.class || c == boolean.class) return new Schema<>().type("boolean");
            if (c.isRecord()) return toSchema(c);
        }
        return new Schema<>().type("object");
    }
}
