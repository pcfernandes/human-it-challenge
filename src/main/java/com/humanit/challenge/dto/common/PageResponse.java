package com.humanit.challenge.dto.common;

import java.util.List;

import org.springframework.data.domain.Page;
import io.swagger.v3.oas.annotations.media.Schema;

/** Minimal, stable pagination metadata for collection responses. */
@Schema(description = "Zero-based page content and pagination metadata.")
public record PageResponse<T>(
        @Schema(description = "Items on this page.")
        List<T> content,
        @Schema(description = "Zero-based index of this page.")
        int page,
        @Schema(description = "Number of items requested per page.")
        int size,
        @Schema(description = "Total number of matching items across all pages.")
        long totalElements,
        @Schema(description = "Total number of pages.")
        int totalPages) {

    public static <T> PageResponse<T> from(Page<T> source) {
        return new PageResponse<>(source.getContent(), source.getNumber(), source.getSize(),
                source.getTotalElements(), source.getTotalPages());
    }
}
