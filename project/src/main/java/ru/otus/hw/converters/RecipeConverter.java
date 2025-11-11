package ru.otus.hw.converters;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.hw.dto.*;
import ru.otus.hw.models.Recipe;
import ru.otus.hw.util.MessageProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RecipeConverter {

    private final CategoryConverter categoryConverter;
    private final AuthorConverter authorConverter;
    private final InventoryConverter inventoryConverter;
    private final MessageProvider messageProvider;

    public RecipeDto toDto(Recipe recipe) {
        if (recipe == null) return null;

        return new RecipeDto(
                recipe.getId(),
                recipe.getTitle(),
                categoryConverter.toDto(recipe.getCategory()),
                authorConverter.toDto(recipe.getAuthor()),
                recipe.getInventoryItems() != null ?
                        inventoryConverter.toDtoList(recipe.getInventoryItems()) : new ArrayList<>(),
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
                recipe.getCategory() != null ? recipe.getCategory().getName() :
                        messageProvider.getMessage("recipe.category.uncategorized"),
                recipe.getAuthor() != null && recipe.getAuthor().getUser() != null ?
                        recipe.getAuthor().getUser().getUsername() :
                        messageProvider.getMessage("recipe.author.unknown"),
                recipe.getComments() != null ? recipe.getComments().size() : 0,
                recipe.isPublished(),
                recipe.getCreatedAt(),
                recipe.getUpdatedAt()
        );
    }

    public RecipeDetailsDto toDetailsDto(Recipe recipe) {
        if (recipe == null) return null;

        return new RecipeDetailsDto(
                recipe.getId(),
                recipe.getTitle(),
                categoryConverter.toDto(recipe.getCategory()),
                authorConverter.toDto(recipe.getAuthor()),
                recipe.getInventoryItems() != null ?
                        inventoryConverter.toDtoList(recipe.getInventoryItems()) : new ArrayList<>(),
                recipe.getIngredients() != null ? recipe.getIngredients() : new ArrayList<>(),
                recipe.getDescription(),
                new ArrayList<>(), // comments would be added separately
                recipe.isPublished(),
                recipe.getCreatedAt(),
                recipe.getUpdatedAt()
        );
    }

    public List<RecipeDto> toDtoList(List<Recipe> recipes) {
        return recipes.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<RecipeSummaryDto> toSummaryDtoList(List<Recipe> recipes) {
        return recipes.stream()
                .map(this::toSummaryDto)
                .collect(Collectors.toList());
    }

    public RecipeDto toDtoFromDetails(RecipeWithDetailsDto details) {
        if (details == null || details.recipe() == null) return null;

        return new RecipeDto(
                details.recipe().id(),
                details.recipe().title(),
                details.recipe().category(),
                details.recipe().author(),
                details.inventoryItems(),
                details.recipe().ingredients(),
                details.recipe().description(),
                details.totalCommentCount(),
                details.recipe().published(),
                details.recipe().createdAt(),
                details.recipe().updatedAt()
        );
    }
}