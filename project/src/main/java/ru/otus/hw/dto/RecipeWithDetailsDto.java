package ru.otus.hw.dto;

import java.util.List;

public record RecipeWithDetailsDto(
        RecipeDto recipe,
        List<InventoryDto> inventoryItems,
        List<CommentDto> comments,
        int totalCommentCount
) {}