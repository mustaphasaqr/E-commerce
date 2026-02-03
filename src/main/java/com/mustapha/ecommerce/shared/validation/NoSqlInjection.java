package com.mustapha.ecommerce.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NoSqlInjectionValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NoSqlInjection {
    String message() default "Input contains potentially malicious SQL patterns";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
