package dev.acton.core.bind;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MapReturn {
  Class<?> value();
  Class<? extends ResultMapper<?, ?>> using() default ResultMapper.Auto.class;
}
