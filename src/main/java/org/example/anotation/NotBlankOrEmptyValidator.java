package org.example.anotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NotBlankOrEmptyValidator implements ConstraintValidator<NotBlankOrEmpty,String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if(value==null){
            return true;
        }
        return !value.trim().isEmpty();
    }
}
