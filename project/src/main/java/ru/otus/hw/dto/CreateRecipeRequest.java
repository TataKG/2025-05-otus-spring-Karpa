package ru.otus.hw.dto;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

public record CreateRecipeRequest(
        String title,
        Long categoryId,
        Long authorId,
        List<String> ingredients,
        List<Long> inventoryIds,
        String description,
        boolean published
) {
    public CreateRecipeRequest {
        inventoryIds = (inventoryIds != null) ? inventoryIds : new ArrayList<>();
    }
}
