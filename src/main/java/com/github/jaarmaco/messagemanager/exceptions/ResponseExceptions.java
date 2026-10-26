package com.github.jaarmaco.messagemanager.exceptions;

public abstract class ResponseExceptions {

    public static String MESSAGE_INVALID_USER_CREDENTIALS = "Invalid user credentials.";

    public static String MESSAGE_GROUP_NOT_FOUND = "Message group does not exist.";

    public static String MEMBERSHIP_REQUIRED = "User is not a member of this message group.";

    public static String ROLE_REQUIRED = "User's role in this group does not permit this operation.";

    public static ForbiddenResponseException forbidden() {
        return ForbiddenResponseException.get();
    }

    public static ForbiddenResponseException forbidden(String detail) {
        return ForbiddenResponseException.forDetail(detail);
    }

    public static ForbiddenResponseException invalidUserCredentials() {
        return forbidden(MESSAGE_INVALID_USER_CREDENTIALS);
    }

    public static ForbiddenResponseException membershipRequired() {
        return forbidden(MEMBERSHIP_REQUIRED);
    }

    public static ForbiddenResponseException roleRequired() {
        return forbidden(ROLE_REQUIRED);
    }

    public static UnauthorizedResponseException unauthorized() {
        return UnauthorizedResponseException.get();
    }

    public static UnauthorizedResponseException unauthorized(String detail) {
        return UnauthorizedResponseException.forDetail(detail);
    }

    public static NotFoundResponseException notFound() {
        return NotFoundResponseException.get();
    }
    
    public static NotFoundResponseException notFound(String detail) {
        return NotFoundResponseException.forDetail(detail);
    }

    public static NotFoundResponseException messageGroupNotFound() {
        return notFound(MESSAGE_GROUP_NOT_FOUND);
    }
}

