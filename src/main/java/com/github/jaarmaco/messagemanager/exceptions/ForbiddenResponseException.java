package com.github.jaarmaco.messagemanager.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

public final class ForbiddenResponseException extends ErrorResponseException {

    private ForbiddenResponseException() {
        super(ProblemDetail.forStatus(HttpStatus.FORBIDDEN));
    }

    private ForbiddenResponseException(String detail) {
        super(ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, detail));
    }

    public static ForbiddenResponseException get() {
        return new ForbiddenResponseException();
    }

    public static ForbiddenResponseException forDetail(String detail) {
        return new ForbiddenResponseException(detail);
    }
}

