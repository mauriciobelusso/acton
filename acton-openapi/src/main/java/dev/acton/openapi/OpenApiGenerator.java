package dev.acton.openapi;

import dev.acton.core.actor.Actor;
import dev.acton.core.annotation.Contract;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.Collection;
import java.util.List;

public class OpenApiGenerator {

    public OpenAPI generate(Collection<Actor> actors) {
        OpenAPI api = new OpenAPI()
                .info(new Info()
                        .title("ActOn API")
                        .version("1.0.0")
                        .description("Generated automatically by ActOn OpenAPI"))
                .paths(new Paths())
                .components(new Components())
                .servers(List.of(new Server().url("/")));

        for (Actor actor : actors) {
            for (Method method : actor.getClass().getMethods()) {
                if (!method.getName().equals("on") || method.getParameterCount() == 0) continue;
                Class<?> payloadType = method.getParameters()[0].getType();
                Contract c = payloadType.getAnnotation(Contract.class);
                if (c == null) continue;

                var http = c.http();
                String path = !http.path().isEmpty() ? http.path() : "/" + c.value();
                PathItem.HttpMethod verb = toOpenApiMethod(http.method());

                // request body schema
                Schema<?> requestSchema = schemaFor(payloadType);
                var requestBody = new RequestBody()
                        .content(new Content().addMediaType(http.consumes()[0],
                                new MediaType().schema(requestSchema)));

                // simple 200 response
                var responses = new ApiResponses()
                        .addApiResponse("200", new ApiResponse()
                                .description("Successful operation")
                                .content(new Content().addMediaType(http.produces()[0],
                                        new MediaType().schema(new Schema<>().type("object")))));

                PathItem item = api.getPaths().computeIfAbsent(path, k -> new PathItem());
                item.operation(verb, new io.swagger.v3.oas.models.Operation()
                        .operationId(method.getName())
                        .summary(c.description())
                        .requestBody(requestBody)
                        .responses(responses));
            }
        }

        return api;
    }

    private PathItem.HttpMethod toOpenApiMethod(Contract.Http.Method method) {
        return switch (method) {
            case GET -> PathItem.HttpMethod.GET;
            case POST -> PathItem.HttpMethod.POST;
            case PUT -> PathItem.HttpMethod.PUT;
            case DELETE -> PathItem.HttpMethod.DELETE;
            case PATCH -> PathItem.HttpMethod.PATCH;
        };
    }

    private Schema<?> schemaFor(Class<?> type) {
        Schema<?> schema = new Schema<>();
        schema.setType("object");
        for (RecordComponent rc : type.getRecordComponents()) {
            schema.addProperties(rc.getName(), new Schema<>().type(mapType(rc.getType())));
        }
        return schema;
    }

    private String mapType(Class<?> t) {
        if (Number.class.isAssignableFrom(t) || t.isPrimitive()) return "number";
        if (t == String.class) return "string";
        if (t == Boolean.class || t == boolean.class) return "boolean";
        return "object";
    }
}
