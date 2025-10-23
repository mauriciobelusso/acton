package dev.acton.core.util;

import dev.acton.core.annotation.Contract;

public class ContractUtils {

    private ContractUtils() {
    }

    public static String derivePath(Contract contract) {
        var name = contract.value();
        var http = contract.http();
        return !http.path().isEmpty() ? http.path() : derivePath(name);
    }

    private static String derivePath(String name) {
        String[] parts = name.split("\\.");
        String resource = parts[0];
        String operation = parts.length > 1 ? parts[1] : "post";
        return switch (operation) {
            case "list", "get", "find", "create", "add" -> "/" + resource;
            case "update", "delete", "remove" -> "/" + resource + "/{id}";
            default -> "/" + name;
        };
    }
}
