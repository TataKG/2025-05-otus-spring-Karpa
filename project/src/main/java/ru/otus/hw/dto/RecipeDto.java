package ru.otus.hw.dto;

import java.util.List;

public record RecipeDto(
        Long id,
        String title,
        CategoryDto category,
        AuthorDto author,
        List<InventoryDto> inventoryItems,
        List<String> ingredients,
        String description,
        int commentCount
) {
}
