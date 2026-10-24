package com.github.jaarmaco.messagemanager.exceptions;

import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class DefaultControllerAdvice {

    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ProblemDetail> handleErrorResponse(ErrorResponseException exception) {
        return ResponseEntity
            .status(exception.getProblemDetail().getStatus())
            .body(exception.getProblemDetail());
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ProblemDetail> handleUnexpectedException(Throwable e) {
        log.error("Uncaught exception in controller: {}", e);
        return ResponseEntity
            .status(500)
            .body(ProblemDetail.forStatus(500));
    }
}

