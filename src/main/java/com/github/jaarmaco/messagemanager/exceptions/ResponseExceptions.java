package com.github.jaarmaco.messagemanager.exceptions;

public abstract class ResponseExceptions {

    public static String MESSAGE_INVALID_USER_CREDENTIALS = "Invalid user credentials.";

    public static ForbiddenResponseException forbidden() {
        return ForbiddenResponseException.get();
    }

    public static ForbiddenResponseException forbidden(String detail) {
        return ForbiddenResponseException.forDetail(detail);
    }

    public static ForbiddenResponseException invalidUserCredentials() {
        return forbidden(MESSAGE_INVALID_USER_CREDENTIALS);
    }

    public static UnauthorizedResponseException unauthorized() {
        return UnauthorizedResponseException.get();
    }

    public static UnauthorizedResponseException unauthorized(String detail) {
        return UnauthorizedResponseException.forDetail(detail);
    }
}

