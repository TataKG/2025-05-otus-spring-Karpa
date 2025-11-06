package ru.otus.hw.converters;

import org.springframework.stereotype.Component;
import ru.otus.hw.dto.InventoryDto;
import ru.otus.hw.dto.RecipeDto;
import ru.otus.hw.dto.RecipeSummaryDto;
import ru.otus.hw.models.Recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RecipeConverter {

    private final CategoryConverter categoryConverter;
    private final AuthorConverter authorConverter;
    private final InventoryConverter inventoryConverter;

    public RecipeConverter(CategoryConverter categoryConverter,
                           AuthorConverter authorConverter,
                           InventoryConverter inventoryConverter) {
        this.categoryConverter = categoryConverter;
        this.authorConverter = authorConverter;
        this.inventoryConverter = inventoryConverter;
    }

    public RecipeDto toDto(Recipe recipe) {
        if (recipe == null) return null;

        List<InventoryDto> inventoryDtos = recipe.getInventoryItems().stream()
                .map(inventoryConverter::toDto)
                .collect(Collectors.toList());

        return new RecipeDto(
                recipe.getId(),
                recipe.getTitle(),
                categoryConverter.toDto(recipe.getCategory()),
                authorConverter.toDto(recipe.getAuthor()),
                inventoryDtos,
                recipe.getIngredients() != null ? recipe.getIngredients() : new ArrayList<>(),
                recipe.getDescription(),
                recipe.getComments() != null ? recipe.getComments().size() : 0
        );
    }

    public RecipeSummaryDto toSummaryDto(Recipe recipe) {
        if (recipe == null) return null;

        return new RecipeSummaryDto(
                recipe.getId(),
                recipe.getTitle(),
                recipe.getCategory() != null ? recipe.getCategory().getName() : "Без категории",
                recipe.getAuthor() != null && recipe.getAuthor().getUser() != null
                        ? recipe.getAuthor().getUser().getUsername() : "Неизвестный автор",
                recipe.getComments() != null ? recipe.getComments().size() : 0
        );
    }
}
