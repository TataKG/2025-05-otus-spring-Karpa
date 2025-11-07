package ru.otus.hw.dto;

import java.time.LocalDateTime;

public record InventoryWithUsageDto(
        Long id,
        String name,
        String description,
        LocalDateTime createdAt,
        boolean usedInRecipes,
        long recipeCount
) {}