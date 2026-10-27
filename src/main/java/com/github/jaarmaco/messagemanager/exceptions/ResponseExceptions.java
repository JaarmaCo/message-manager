package com.github.jaarmaco.messagemanager.exceptions;

import com.github.jaarmaco.messagemanager.lib.pagination.Pagination;

public abstract class ResponseExceptions {

    public static String MESSAGE_INVALID_USER_CREDENTIALS = "Invalid user credentials.";

    public static String MESSAGE_GROUP_NOT_FOUND = "Message group does not exist.";

    public static String MESSAGE_NOT_FOUND = "There is no message associated with the provided id.";

    public static String MEMBERSHIP_REQUIRED = "User is not a member of this message group.";

    public static String ROLE_REQUIRED = "User's role in this group does not permit this operation.";

    public static String NON_POSITIVE_PAGE_NUMBER = "Page number has to be larger than 0.";

    public static String NON_POSITIVE_PAGE_SIZE = "Page size has to be larger than 0.";

    public static String PAGE_SIZE_TOO_LARGE = "Page size has to be less than or equal to " + Pagination.MAX_PAGE_SIZE + ".";

    public static String PAGE_NUMBER_IS_REQUIRED = "Use of a paginated endpoint requires that you specify a page number.";

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

    public static NotFoundResponseException messageNotFound() {
        return notFound(MESSAGE_NOT_FOUND);
    }

    public static BadRequestResponseException badRequest() {
        return BadRequestResponseException.get();
    }

    public static BadRequestResponseException badRequest(String detail) {
        return BadRequestResponseException.forDetail(detail);
    }
    
    public static BadRequestResponseException pageNumberIsRequired() {
        return badRequest(PAGE_NUMBER_IS_REQUIRED);
    }

    public static BadRequestResponseException nonPositivePageNumber() {
        return badRequest(NON_POSITIVE_PAGE_NUMBER);
    }

    public static BadRequestResponseException nonPositivePageSize() {
        return badRequest(NON_POSITIVE_PAGE_SIZE);
    }

    public static BadRequestResponseException pageSizeTooLarge() {
        return badRequest(PAGE_SIZE_TOO_LARGE);
    }
}

