package dev.acton.core.annotation;

import dev.acton.core.bind.ContractMapper;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FromContract {
    Class<? extends ContractMapper<?, ?>> using() default ContractMapper.Auto.class;
    Class<?> to() default Void.class;
}
