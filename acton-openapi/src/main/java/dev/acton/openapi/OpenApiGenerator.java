package dev.acton.openapi;

import dev.acton.core.annotation.Contract;
import dev.acton.router.RouteBinding;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Builds an OpenAPI model from ActOn RouteBinding entries.
 * Rules:
 *  - HTTP method & path: read from @Contract(http=...), otherwise derived from the contract name (e.g., "orders.list" -> GET /orders).
 *  - Request body: inferred from the single parameter of Actor.on(...). GET requests omit body.
 *  - Response: inferred from return type. Supports void/Optional<T>/Collection<T>/Page<T>.
 *  - Schemas: resolved via swagger-core ModelConverters (Jakarta variant).
 */
public final class OpenApiGenerator {

    // ---- Public API ---------------------------------------------------------

    public OpenAPI generate(Collection<RouteBinding> routes, String title, String version, String serverUrl) {
        OpenAPI api = new OpenAPI()
                .info(new Info().title(title).version(version))
                .servers(List.of(new io.swagger.v3.oas.models.servers.Server().url(serverUrl)))
                .components(new Components())
                .paths(new io.swagger.v3.oas.models.Paths());

        for (RouteBinding rb : routes) {
            PathItem.HttpMethod httpMethod = httpMethodOf(rb);
            String path = httpPathOf(rb);

            var pathItem = api.getPaths().computeIfAbsent(path, p -> new PathItem());
            var op = new io.swagger.v3.oas.models.Operation()
                    .operationId(safeOpId(rb.name(), httpMethod.name(), path))
                    .summary(rb.name());

            // Request body (skip for GET)
            if (httpMethod != PathItem.HttpMethod.GET) {
                Schema<?> reqSchema = requestSchemaOf(rb, api.getComponents());
                if (reqSchema != null) {
                    MediaType mt = new MediaType().schema(reqSchema);
                    var body = new RequestBody()
                            .required(true)
                            .content(new io.swagger.v3.oas.models.media.Content().addMediaType("application/json", mt));
                    op.requestBody(body);
                }
            }

            // Responses
            ApiResponses responses = new ApiResponses();
            Schema<?> respSchema = responseSchemaOf(rb, api.getComponents());
            if (respSchema == null) {
                responses.addApiResponse("204", new ApiResponse().description("No Content"));
            } else {
                MediaType mt = new MediaType().schema(respSchema);
                responses.addApiResponse("200",
                        new ApiResponse().description("OK")
                                .content(new io.swagger.v3.oas.models.media.Content()
                                        .addMediaType("application/json", mt)));
            }
            op.responses(responses);

            switch (httpMethod) {
                case GET -> pathItem.get(op);
                case POST -> pathItem.post(op);
                case PUT -> pathItem.put(op);
                case DELETE -> pathItem.delete(op);
                case PATCH -> pathItem.patch(op);
                case HEAD -> pathItem.head(op);
                case OPTIONS -> pathItem.options(op);
                case TRACE -> pathItem.trace(op);
            }
        }

        return api;
    }

    // ---- Request / Response schema inference -------------------------------

    private Schema<?> requestSchemaOf(RouteBinding rb, Components components) {
        Type paramType = rb.method().getParameters()[0].getParameterizedType();

        if (isOptional(paramType)) {
            Type inner = firstTypeArg(paramType).orElse(Object.class);
            return schemaFor(erase(inner), components);
        }
        if (isCollection(paramType)) {
            Type elem = firstTypeArg(paramType).orElse(Object.class);
            ArraySchema arr = new ArraySchema();
            arr.setItems(schemaFor(erase(elem), components));
            return arr;
        }
        return schemaFor(erase(paramType), components);
    }

    private Schema<?> responseSchemaOf(RouteBinding rb, Components components) {
        Type retType = rb.method().getGenericReturnType();
        Class<?> raw = erase(retType);

        // void -> 204
        if (raw == Void.TYPE || raw == Void.class) return null;

        // Optional<T> -> T
        if (isOptional(retType)) {
            Type inner = firstTypeArg(retType).orElse(Object.class);
            return schemaFor(erase(inner), components);
        }

        // Page<T> -> { items: T[], total: long, page: int, size: int }
        if (isPage(raw)) {
            Type inner = firstTypeArg(retType).orElse(Object.class);
            Class<?> elem = erase(inner);

            ArraySchema items = new ArraySchema().items(schemaFor(elem, components));
            return new ObjectSchema()
                    .addProperty("items", items)
                    .addProperty("total", new IntegerSchema().format("int64"))
                    .addProperty("page", new IntegerSchema())
                    .addProperty("size", new IntegerSchema());
        }

        // Collection<T> -> T[]
        if (isCollection(retType)) {
            Type elem = firstTypeArg(retType).orElse(Object.class);
            ArraySchema arr = new ArraySchema();
            arr.setItems(schemaFor(erase(elem), components));
            return arr;
        }

        // Plain type
        return schemaFor(raw, components);
    }

    private boolean isPage(Class<?> raw) {
        // match by FQCN to avoid coupling
        return "dev.acton.core.store.Page".equals(raw.getName());
    }

    // ---- HTTP method & path derivation -------------------------------------

    private PathItem.HttpMethod httpMethodOf(RouteBinding rb) {
        Class<?> payload = rb.method().getParameters()[0].getType();
        Contract c = payload.getAnnotation(Contract.class);
        if (c != null && c.http() != null) {
            return switch (c.http().method()) {
                case GET -> PathItem.HttpMethod.GET;
                case POST -> PathItem.HttpMethod.POST;
                case PUT -> PathItem.HttpMethod.PUT;
                case DELETE -> PathItem.HttpMethod.DELETE;
                case PATCH -> PathItem.HttpMethod.PATCH;
            };
        }
        // derive from name
        String op = opOf(contractName(rb));
        return switch (op) {
            case "list", "get", "find" -> PathItem.HttpMethod.GET;
            case "update"              -> PathItem.HttpMethod.PUT;
            case "delete", "remove"    -> PathItem.HttpMethod.DELETE;
            default                    -> PathItem.HttpMethod.POST;
        };
    }

    private String httpPathOf(RouteBinding rb) {
        Class<?> payload = rb.method().getParameters()[0].getType();
        Contract c = payload.getAnnotation(Contract.class);
        if (c != null && c.http() != null && !c.http().path().isEmpty()) {
            return c.http().path();
        }
        // derive from name
        String name = contractName(rb);
        String res = resourceOf(name);
        String op  = opOf(name);
        return switch (op) {
            case "list", "get", "find" -> "/" + res;
            case "create", "add"       -> "/" + res;
            case "update"              -> "/" + res + "/{id}";
            case "delete", "remove"    -> "/" + res + "/{id}";
            default                    -> "/acton/" + name;
        };
    }

    private String contractName(RouteBinding rb) {
        Class<?> payload = rb.method().getParameters()[0].getType();
        Contract c = payload.getAnnotation(Contract.class);
        return (c != null && !c.value().isBlank()) ? c.value() : rb.name();
    }

    private String resourceOf(String name) {
        String[] p = name.split("\\.");
        return p[0];
    }

    private String opOf(String name) {
        String[] p = name.split("\\.");
        return (p.length > 1) ? p[1] : "post";
    }

    private String safeOpId(String name, String verb, String path) {
        String norm = path.replaceAll("[^a-zA-Z0-9]", "_");
        return name + "_" + verb.toLowerCase() + "_" + norm;
    }

    // ---- Schema resolution helpers -----------------------------------------

    private Schema<?> schemaFor(Class<?> type, Components components) {
        var resolved = ModelConverters.getInstance()
                .resolveAsResolvedSchema(new AnnotatedType(type).resolveAsRef(true));
        if (resolved.schema != null) {
            if (resolved.referencedSchemas != null && components != null) {
                resolved.referencedSchemas.forEach(components::addSchemas);
            }
            return resolved.schema;
        }
        // Fallback generic object
        return new ObjectSchema();
    }

    private static boolean isOptional(Type t) {
        return erase(t) == Optional.class;
    }

    private static boolean isCollection(Type t) {
        return Collection.class.isAssignableFrom(erase(t));
    }

    private static Optional<Type> firstTypeArg(Type t) {
        if (t instanceof ParameterizedType p && p.getActualTypeArguments().length >= 1) {
            return Optional.ofNullable(p.getActualTypeArguments()[0]);
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private static Class<?> erase(Type t) {
        if (t instanceof Class<?> c) return c;
        if (t instanceof ParameterizedType p) return (Class<?>) p.getRawType();
        if (t instanceof GenericArrayType) return Object[].class;
        // Defensive fallback:
        return Object.class;
    }
}
