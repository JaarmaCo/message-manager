package com.github.jaarmaco.messagemanager.lib.pagination;

import java.util.List;

import org.springframework.data.domain.Pageable;

import lombok.Builder;

@Builder
public record Paginated<T>(
        int count,
        int pageNumber,
        int pageSize,
        List<T> results) {


    public static <T> Paginated<T> fromPaginatedResults(List<T> results, Pageable pageable) {
        return new Paginated<>(
                results.size(),
                pageable.getPageNumber(),
                pageable.getPageSize(),
                results); 
    }
}

