package dev.acton.spring.util;

import dev.acton.core.annotation.Contract;
import io.swagger.v3.oas.models.PathItem;
import org.springframework.web.bind.annotation.RequestMethod;

public class HttpUtils {

    private HttpUtils() {
    }

    public static String derivePath(String contractName) {
        String[] parts = contractName.split("\\.");
        String resource = parts[0];
        String operation = parts.length > 1 ? parts[1] : "post";
        return switch (operation) {
            case "list", "get", "find", "create", "add" -> "/" + resource;
            case "update", "delete", "remove" -> "/" + resource + "/{id}";
            default -> "/" + contractName;
        };
    }

    public static RequestMethod toSpringMethod(Contract.Http.Method method) {
        return switch (method) {
            case GET -> RequestMethod.GET;
            case POST -> RequestMethod.POST;
            case PUT -> RequestMethod.PUT;
            case DELETE -> RequestMethod.DELETE;
            case PATCH -> RequestMethod.PATCH;
        };
    }

    public static PathItem.HttpMethod toOasMethod(Contract.Http.Method method) {
        return switch (method) {
            case GET -> PathItem.HttpMethod.GET;
            case POST -> PathItem.HttpMethod.POST;
            case PUT -> PathItem.HttpMethod.PUT;
            case DELETE -> PathItem.HttpMethod.DELETE;
            case PATCH -> PathItem.HttpMethod.PATCH;
        };
    }

    public static String pickFirstOrDefault(String[] array, String defaultValue) {
        return (array != null && array.length > 0 && array[0] != null && !array[0].isBlank()) 
            ? array[0] : defaultValue;
    }
}
