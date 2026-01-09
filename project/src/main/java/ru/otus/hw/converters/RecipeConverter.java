package ru.otus.hw.converters;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.hw.dto.*;
import ru.otus.hw.models.Recipe;

import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class RecipeConverter {

    private final CategoryConverter categoryConverter;
    private final AuthorConverter authorConverter;
    private final InventoryConverter inventoryConverter;

    public RecipeDto toDto(Recipe recipe) {
        if (recipe == null) return null;

        return new RecipeDto(
                recipe.getId(),
                recipe.getTitle(),
                categoryConverter.toDto(recipe.getCategory()),
                authorConverter.toDto(recipe.getAuthor()),
                inventoryConverter.toDtoList(recipe.getInventoryItems()),
                recipe.getIngredients() != null ? recipe.getIngredients() : new ArrayList<>(),
                recipe.getDescription(),
                recipe.getComments() != null ? recipe.getComments().size() : 0,
                recipe.isPublished(),
                recipe.getCreatedAt(),
                recipe.getUpdatedAt()
        );
    }

    public RecipeSummaryDto toSummaryDto(Recipe recipe) {
        if (recipe == null) return null;

        return new RecipeSummaryDto(
                recipe.getId(),
                recipe.getTitle(),
                recipe.getCategory().getName(),
                recipe.getAuthor().getUser().getUsername(),
                recipe.getComments().size(),
                recipe.isPublished(),
                recipe.getCreatedAt()
        );
    }
}