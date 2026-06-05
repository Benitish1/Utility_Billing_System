package com.wasac.billing.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AccessDeniedException extends RuntimeException {

    private final HttpStatus status = HttpStatus.FORBIDDEN;

    public AccessDeniedException(String message) {
        super(message);
    }
}
