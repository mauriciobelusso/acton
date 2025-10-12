package dev.acton.core.annotation;

import java.lang.annotation.*;

/**
 * Declares a contract that defines an operation in the ActOn architecture.
 * A contract represents the intent of an action, similar to an endpoint or message.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Contract {
    /**
     * Unique name of the contract (e.g. "orders.create").
     */
    String value();

    /**
     * Optional description for documentation or OpenAPI generation.
     */
    String description() default "";
}
