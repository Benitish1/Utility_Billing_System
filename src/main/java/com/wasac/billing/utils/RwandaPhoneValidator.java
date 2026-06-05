package com.wasac.billing.utils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class RwandaPhoneValidator implements ConstraintValidator<RwandaPhone, String> {

    private static final Pattern RWANDA_PHONE = Pattern.compile("^(\\+250|250|0)?7[2389]\\d{7}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String normalized = value.replaceAll("\\s", "");
        return RWANDA_PHONE.matcher(normalized).matches();
    }

    public static String normalize(String phone) {
        String cleaned = phone.replaceAll("\\s", "");
        if (cleaned.startsWith("+250")) {
            return cleaned;
        }
        if (cleaned.startsWith("250")) {
            return "+" + cleaned;
        }
        if (cleaned.startsWith("0")) {
            return "+250" + cleaned.substring(1);
        }
        return cleaned;
    }
}
