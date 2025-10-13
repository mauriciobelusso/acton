package dev.acton.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as an ActOn actor.
 * Any public method whose first parameter is a @Contract record
 * will be automatically registered as an HTTP endpoint.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Actor {

    /**
     * Optional name for debugging or grouping.
     * Defaults to the simple class name.
     */
    String value() default "";
}
