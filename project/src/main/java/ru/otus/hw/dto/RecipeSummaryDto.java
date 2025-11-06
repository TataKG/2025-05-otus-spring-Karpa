package ru.otus.hw.dto;

public record RecipeSummaryDto(
        Long id,
        String title,
        String categoryName,
        String authorName,
        int commentCount
) {
}
