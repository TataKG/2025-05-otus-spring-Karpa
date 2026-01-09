package ru.otus.hw.dto;

import java.time.LocalDateTime;

public record RecipeSummaryDto(
        Long id,
        String title,
        String categoryName,
        String authorName,
        int commentCount,
        boolean published,
        LocalDateTime createdAt
) {
}
