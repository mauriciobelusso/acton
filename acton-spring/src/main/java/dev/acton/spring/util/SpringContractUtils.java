package dev.acton.spring.util;

import dev.acton.core.annotation.Contract;
import org.springframework.web.bind.annotation.RequestMethod;

public class SpringContractUtils {

    private SpringContractUtils() {
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
}
