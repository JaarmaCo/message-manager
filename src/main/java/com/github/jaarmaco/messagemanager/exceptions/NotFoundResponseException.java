package com.github.jaarmaco.messagemanager.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

public final class NotFoundResponseException extends ErrorResponseException {

    private NotFoundResponseException() {
        super(ProblemDetail.forStatus(HttpStatus.NOT_FOUND));
    }

    private NotFoundResponseException(String detail) {
        super(ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, detail));
    }

    public static NotFoundResponseException get() {
        return new NotFoundResponseException();
    }

    public static NotFoundResponseException forDetail(String detail) {
        return new NotFoundResponseException(detail);
    }
}

