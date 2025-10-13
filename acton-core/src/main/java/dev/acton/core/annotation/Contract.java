package dev.acton.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Contract {
    /** Name like "orders.create" or "orders.list". */
    String value();

    String description() default "";

    /** Optional HTTP override; omit for name-based defaults. */
    Http http() default @Http;

    enum Method { GET, POST, PUT, DELETE, PATCH }

    @Target({})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Http {
        Method method() default Method.POST;   // default if provided without fields
        String path() default "";              // empty ⇒ derive from name
    }
}
