package com.github.jaarmaco.messagemanager.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

public final class BadRequestResponseException extends ErrorResponseException {

    private BadRequestResponseException() {
        super(ProblemDetail.forStatus(HttpStatus.BAD_REQUEST));
    }

    private BadRequestResponseException(String detail) {
        super(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail));
    }

    public static BadRequestResponseException get() {
        return new BadRequestResponseException();
    }

    public static BadRequestResponseException forDetail(String detail) {
        return new BadRequestResponseException(detail);
    }
}
