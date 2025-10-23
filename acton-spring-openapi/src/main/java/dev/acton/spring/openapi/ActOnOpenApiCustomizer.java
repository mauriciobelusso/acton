package dev.acton.spring.openapi;

import dev.acton.core.annotation.Contract;
import dev.acton.core.util.ContractUtils;
import dev.acton.spring.bind.BinderSpringHandler;
import dev.acton.spring.util.TypeUtils;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.core.annotation.AnnotatedElementUtils;
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

        Map<String, String> rootTags = existingRootTags(openApi);

        mapping.getHandlerMethods().forEach((info, handler) -> {
            if (!isActOnHandler(handler)) return;

            BinderSpringHandler<?> invoker = (BinderSpringHandler<?>) handler.getBean();
            Class<?> contractType = invoker.contractType();
            Contract c = contractType.getAnnotation(Contract.class);
            if (c == null) return;

            var http = c.http();
            String path = (http.path().isEmpty()) ? ContractUtils.derivePath(c) : http.path();
            PathItem.HttpMethod verb = toOasMethod(http.method());

            PathItem pathItem = openApi.getPaths().computeIfAbsent(path, k -> new PathItem());
            Operation op = new Operation()
                    .summary(c.description().isEmpty() ? c.value() : c.description())
                    .operationId(buildOpId(handler.getMethod(), c.value()));

            // ---------- TAGS ----------
            applyTags(contractType, op, openApi, rootTags);
            // --------------------------

            boolean hasBody = switch (http.method()) { case POST, PUT, PATCH -> true; default -> false; };
            if (hasBody) {
                Schema<?> schema = toSchema(contractType);
                String consumes = pickFirstOrDefault(http.consumes(), "application/json");
                op.requestBody(new RequestBody()
                        .content(new Content().addMediaType(consumes, new MediaType().schema(schema))));
            }

            Type rtype = (invoker.returnType() != null) ? invoker.returnType() : Object.class;
            Schema<?> respSchema = toResponseSchema(rtype);
            String produces = pickFirstOrDefault(http.produces(), "application/json");
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
        return hm.getBean() instanceof BinderSpringHandler<?>;
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
        Type t = TypeUtils.unwrap(type);
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

    private Schema<?> primitiveOrObject(Type t) {
        if (t instanceof Class<?> c) {
            if (Number.class.isAssignableFrom(c) || c.isPrimitive()) return new Schema<>().type("number");
            if (c == String.class || CharSequence.class.isAssignableFrom(c)) return new Schema<>().type("string");
            if (c == Boolean.class || c == boolean.class) return new Schema<>().type("boolean");
            if (c.isRecord()) return toSchema(c);
        }
        return new Schema<>().type("object");
    }

    private PathItem.HttpMethod toOasMethod(Contract.Http.Method method) {
        return switch (method) {
            case GET -> PathItem.HttpMethod.GET;
            case POST -> PathItem.HttpMethod.POST;
            case PUT -> PathItem.HttpMethod.PUT;
            case DELETE -> PathItem.HttpMethod.DELETE;
            case PATCH -> PathItem.HttpMethod.PATCH;
        };
    }

    private String pickFirstOrDefault(String[] array, String defaultValue) {
        return (array != null && array.length > 0 && array[0] != null && !array[0].isBlank())
                ? array[0] : defaultValue;
    }

    // ====== TAGS ======

    private void applyTags(Class<?> contractType, Operation op, OpenAPI openApi, Map<String, String> rootTags) {
        var tags = AnnotatedElementUtils.findAllMergedAnnotations(
                contractType, io.swagger.v3.oas.annotations.tags.Tag.class);

        if (!tags.isEmpty()) {
            List<String> names = tags.stream().map(io.swagger.v3.oas.annotations.tags.Tag::name).toList();
            op.setTags(names);
            for (var t : tags) {
                String name = t.name();
                String desc = (t.description() == null || t.description().isBlank()) ? null : t.description();
                if (!rootTags.containsKey(name)) {
                    openApi.addTagsItem(new io.swagger.v3.oas.models.tags.Tag().name(name).description(desc));
                    rootTags.put(name, desc);
                }
            }
        } else {
            String kebab = toKebabCase(contractType.getSimpleName());
            op.setTags(List.of(kebab));
            if (!rootTags.containsKey(kebab)) {
                openApi.addTagsItem(new io.swagger.v3.oas.models.tags.Tag().name(kebab));
                rootTags.put(kebab, null);
            }
        }
    }

    private Map<String, String> existingRootTags(OpenAPI openApi) {
        Map<String, String> map = new LinkedHashMap<>();
        if (openApi.getTags() != null) {
            for (var t : openApi.getTags()) {
                map.put(t.getName(), t.getDescription());
            }
        }
        return map;
    }

    private String toKebabCase(String name) {
        return name
                .replaceAll("([a-z])([A-Z])", "$1-$2")
                .replaceAll("([A-Z])([A-Z][a-z])", "$1-$2")
                .toLowerCase();
    }
}
