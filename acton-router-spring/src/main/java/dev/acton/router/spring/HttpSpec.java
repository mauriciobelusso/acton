package dev.acton.router.spring;

import dev.acton.core.annotation.Contract;
import dev.acton.router.RouteBinding;
import org.springframework.web.bind.annotation.RequestMethod;

record HttpSpec(RequestMethod method, String path) {
    static HttpSpec from(RouteBinding rb) {
        Class<?> payload = rb.method().getParameters()[0].getType();
        Contract c = payload.getAnnotation(Contract.class);
        if (c == null) throw new IllegalStateException("Missing @Contract on " + payload.getName());

        // If user provided http() customization
        Contract.Http http = c.http();
        if (http != null && (!http.path().isEmpty() || http.method() != null)) {
            RequestMethod m = toSpring(http.method());
            String p = !http.path().isEmpty() ? http.path() : derivePath(c.value());
            return new HttpSpec(m, p);
        }

        // Fallback: derive from name
        return deriveByName(c.value());
    }

    private static RequestMethod toSpring(Contract.Method m) {
        return switch (m) {
            case GET -> RequestMethod.GET;
            case POST -> RequestMethod.POST;
            case PUT -> RequestMethod.PUT;
            case DELETE -> RequestMethod.DELETE;
            case PATCH -> RequestMethod.PATCH;
        };
    }

    private static HttpSpec deriveByName(String name) {
        return new HttpSpec(guessMethod(name), derivePath(name));
    }

    private static RequestMethod guessMethod(String name) {
        String op = op(name);
        return switch (op) {
            case "list","get","find" -> RequestMethod.GET;
            case "update"            -> RequestMethod.PUT;
            case "delete","remove"   -> RequestMethod.DELETE;
            default                   -> RequestMethod.POST;
        };
    }

    private static String derivePath(String name) {
        String res = resource(name);
        String op  = op(name);
        return switch (op) {
            case "list", "get", "find", "create", "add" -> "/" + res;
            case "update", "delete", "remove" -> "/" + res + "/{id}";
            default                   -> "/acton/" + name;
        };
    }

    private static String resource(String name) {
        String[] parts = name.split("\\.");
        return parts[0];
    }
    private static String op(String name) {
        String[] parts = name.split("\\.");
        return parts.length > 1 ? parts[1] : "post";
    }
}
