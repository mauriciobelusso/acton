package dev.acton.core.annotation;

import java.lang.annotation.*;

/**
 * Optional annotation for HTTP exposure metadata.
 * This is used by ActOn Router modules to auto-generate routes and OpenAPI specs.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Http {

    Method method() default Method.GET;

    String path() default "";

    enum Method {
        GET, POST, PUT, DELETE, PATCH
    }
}
