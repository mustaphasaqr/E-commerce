package com.mustapha.ecommerce.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class NoSqlInjectionValidator implements ConstraintValidator<NoSqlInjection, String> {
    
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "('.*(--|;|/\\*|\\*/|xp_|sp_|exec|execute|select|insert|update|delete|drop|create|alter|union))|(\\bor\\b.*=.*=)|(\\band\\b.*=.*=)",
        Pattern.CASE_INSENSITIVE
    );
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        
        return !SQL_INJECTION_PATTERN.matcher(value).find();
    }
}
