package ru.otus.hw.dto;

import java.time.LocalDateTime;

public record CategoryWithUsageDto(
        Long id,
        String name,
        String description,
        LocalDateTime createdAt,
        boolean usedInRecipes,
        long recipeCount
) {
    public boolean canBeDeleted() {
        return recipeCount == 0;
    }
}