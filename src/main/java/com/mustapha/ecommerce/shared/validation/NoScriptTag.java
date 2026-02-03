package com.mustapha.ecommerce.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NoScriptTagValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NoScriptTag {
    String message() default "Input contains potentially malicious script tags";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
