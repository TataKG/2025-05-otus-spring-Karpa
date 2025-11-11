package ru.otus.hw.dto;

import java.time.LocalDateTime;
import java.util.List;

public record RecipeDetailsDto(
        Long id,
        String title,
        CategoryDto category,
        AuthorDto author,
        List<InventoryDto> inventoryItems,
        List<String> ingredients,
        String description,
        List<CommentDto> comments,
        boolean published,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}