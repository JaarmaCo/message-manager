package com.github.jaarmaco.messagemanager.lib.pagination;

import org.springframework.data.domain.Pageable;

import com.github.jaarmaco.messagemanager.exceptions.ResponseExceptions;
import com.github.jaarmaco.messagemanager.lib.functional.Result;

public abstract class Pagination {

    public static int MAX_PAGE_SIZE = 1000;

    public static int DEFAULT_PAGE_SIZE = 25;

    public static Result<Pageable> ofNullableRange(Integer pageNumber, Integer pageSize) {
        if (pageNumber == null) {
            return Result.exception(ResponseExceptions.pageNumberIsRequired());
        }
        return pageSize == null ? ofRange((int)pageNumber) : ofRange((int)pageNumber, (int)pageSize);
    }

    public static Result<Pageable> ofRange(int pageNumber) {
        return ofRange(pageNumber, DEFAULT_PAGE_SIZE);
    }

    public static Result<Pageable> ofRange(int pageNumber, int pageSize) {
        if (pageNumber <= 0) {
            return Result.exception(ResponseExceptions.nonPositivePageNumber());
        }
        if (pageSize <= 0) {
            return Result.exception(ResponseExceptions.nonPositivePageSize());
        }
        if (pageSize > MAX_PAGE_SIZE) {
            return Result.exception(ResponseExceptions.pageSizeTooLarge());
        }
        return Result.of(Pageable.ofSize(pageSize).withPage(pageNumber)); 
    }
}

