package com.wasac.billing.utils;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validates that a customer national ID follows Rwanda's 16-digit numeric format.
 */
@Documented
@Constraint(validatedBy = RwandaNationalIdValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface RwandaNationalId {

    String message() default "Rwanda national ID must contain exactly 16 digits and cannot be all repeated digits";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
