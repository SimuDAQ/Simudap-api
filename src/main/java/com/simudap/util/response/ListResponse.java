package com.simudap.util.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record ListResponse<T, M>(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<T> content,
        M metadata
) {
    public static <R, M> ListResponse<R, M> of(List<R> content, M metadata) {
        return new ListResponse<>(content, metadata);
    }

    public static <R> ListResponse<R, Void> of(List<R> content) {
        return of(content, null);
    }
}
