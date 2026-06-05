package com.wasac.billing.utils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Rejects empty, non-numeric, wrong-length, and obviously fake Rwanda IDs.
 */
public class RwandaNationalIdValidator implements ConstraintValidator<RwandaNationalId, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String trimmed = value.trim();
        if (!trimmed.matches("^\\d{16}$")) {
            return false;
        }
        return !trimmed.matches("^(\\d)\\1{15}$");
    }
}
