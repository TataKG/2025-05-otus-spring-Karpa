package ru.otus.hw.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CategoryWithRecipesDto(
        Long id,
        String name,
        String description,
        LocalDateTime createdAt,
        List<RecipeInfoDto> recipes
) {
}
