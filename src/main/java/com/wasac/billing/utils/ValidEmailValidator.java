package com.wasac.billing.utils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class ValidEmailValidator implements ConstraintValidator<ValidEmail, String> {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return false;
        }
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            return false;
        }
        String localPart = value.split("@")[0];
        if (localPart.length() < 3) {
            return false;
        }
        return !localPart.matches("^[0-9+\\-()\\s]+$");
    }
}
