package com.wasac.billing.utils;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = RwandaPhoneValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface RwandaPhone {

    String message() default "Phone number must be a valid Rwanda format (e.g. +250788123456 or 0788123456)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
