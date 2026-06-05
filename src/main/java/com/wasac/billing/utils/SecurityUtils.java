package com.wasac.billing.utils;

import com.wasac.billing.entity.User;
import com.wasac.billing.exception.UnauthorizedException;
import com.wasac.billing.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails details)) {
            throw new UnauthorizedException("No authenticated user found");
        }
        return details.getUser();
    }
}
