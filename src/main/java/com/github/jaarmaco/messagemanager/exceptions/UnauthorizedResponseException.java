package com.github.jaarmaco.messagemanager.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

public final class UnauthorizedResponseException extends ErrorResponseException {

    private UnauthorizedResponseException() {
        super(ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED));
    }

    private UnauthorizedResponseException(String detail) {
        super(ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, detail));
    }

    public static UnauthorizedResponseException get() {
        return new UnauthorizedResponseException();
    }

    public static UnauthorizedResponseException forDetail(String detail) {
        return new UnauthorizedResponseException(detail);
    }
}

