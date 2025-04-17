package org.example.anotation;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;


@Documented
@Constraint(validatedBy = NotBlankOrEmptyValidator.class)
@Target({ElementType.FIELD , ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotBlankOrEmpty {
    String message() default "El campo no puede estar vacío o en blanco";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
