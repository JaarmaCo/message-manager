package com.github.jaarmaco.messagemanager.exceptions;

public abstract class ResponseExceptions {

    public static ForbiddenResponseException forbidden() {
        return ForbiddenResponseException.get();
    }

    public static ForbiddenResponseException forbidden(String detail) {
        return ForbiddenResponseException.forDetail(detail);
    }

    public static ForbiddenResponseException invalidUserCredentials() {
        return forbidden("Invalid user credentials.");
    }

    public static UnauthorizedResponseException unauthorized() {
        return UnauthorizedResponseException.get();
    }

    public static UnauthorizedResponseException unauthorized(String detail) {
        return UnauthorizedResponseException.forDetail(detail);
    }
}

