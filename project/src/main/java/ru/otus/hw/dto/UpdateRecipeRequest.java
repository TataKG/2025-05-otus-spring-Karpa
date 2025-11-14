package ru.otus.hw.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateRecipeRequest(
        @NotBlank(message = "{recipe.title.required}")
        @Size(min = 2, max = 255, message = "{recipe.title.min_length}")
        String title,

        @NotNull(message = "{recipe.category.required}")
        Long categoryId,

        @NotNull(message = "{author.id.null}")
        Long authorId,

        @NotEmpty(message = "{recipe.ingredients.empty}")
        List<@NotBlank(message = "{recipe.ingredient.blank}") String> ingredients,

        @NotBlank(message = "{recipe.description.required}")
        @Size(min = 10, max = 5000, message = "{recipe.description.min_length}")
        String description,

        List<Long> inventoryIds,

        boolean published
) {
}
