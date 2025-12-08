package com.simudap.util.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T, M>(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<T> content,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        PageMeta page,
        M metadata
) {

    public static <R, M> PageResponse<R, M> of(Page<R> page, M metadata) {
        return new PageResponse<>(page.stream().toList(), PageMeta.of(page), metadata);
    }

    public static <R> PageResponse<R, Void> of(Page<R> page) {
        return of(page, null);
    }

    public record PageMeta(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            int size,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            int page,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            boolean first,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            boolean last,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            int numberOfElements,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            long totalElements,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            long totalPages
    ) {
        public static <R> PageMeta of(Page<R> page) {
            return new PageMeta(
                    page.getSize(),
                    page.getNumber(),
                    page.isFirst(),
                    page.isLast(),
                    page.getNumberOfElements(),
                    page.getTotalElements(),
                    page.getTotalPages()
            );
        }
    }
}