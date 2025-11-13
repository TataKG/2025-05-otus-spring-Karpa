package ru.otus.hw.dto;

import java.time.LocalDateTime;

public record RecipeInfoDto(
        Long id,
        String title,
        boolean published,
        LocalDateTime createdAt
) {
}