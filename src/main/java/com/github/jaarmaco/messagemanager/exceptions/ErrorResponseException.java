package com.github.jaarmaco.messagemanager.exceptions;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class ErrorResponseException extends RuntimeException {

    private final ProblemDetail problemDetail;

    protected ErrorResponseException(ProblemDetail problemDetail) {
        super("Requested controller operation failed.");
        this.problemDetail = problemDetail;
    }

    public static ErrorResponseException of(@NonNull ProblemDetail problemDetail) {
        return new ErrorResponseException(problemDetail);
    }

    public static ErrorResponseException forStatus(int status) {
        return of(ProblemDetail.forStatus(status));
    }

    public static ErrorResponseException forStatus(@NonNull HttpStatusCode status) {
        return of(ProblemDetail.forStatus(status));
    }

    public static ErrorResponseException forStatusAndDetail(@NonNull HttpStatusCode status, String detail) {
        return of(ProblemDetail.forStatusAndDetail(status, detail));
    }
}

