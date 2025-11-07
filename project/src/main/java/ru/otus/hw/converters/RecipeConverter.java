package ru.otus.hw.converters;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.hw.dto.*;
import ru.otus.hw.models.Recipe;
import ru.otus.hw.services.CommentService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RecipeConverter {

    private final CategoryConverter categoryConverter;
    private final AuthorConverter authorConverter;
    private final InventoryConverter inventoryConverter;
    private final CommentConverter commentConverter;
    private final CommentService commentService;

    public RecipeDto toDto(Recipe recipe) {
        if (recipe == null) return null;

        List<InventoryDto> inventoryDtos = recipe.getInventoryItems() != null
                ? recipe.getInventoryItems().stream()
                .map(inventoryConverter::toDto)
                .collect(Collectors.toList())
                : new ArrayList<>();

        return new RecipeDto(
                recipe.getId(),
                recipe.getTitle(),
                categoryConverter.toDto(recipe.getCategory()),
                authorConverter.toDto(recipe.getAuthor()),
                inventoryDtos,
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
                recipe.getCategory() != null ? recipe.getCategory().getName() : "Без категории",
                recipe.getAuthor() != null && recipe.getAuthor().getUser() != null
                        ? recipe.getAuthor().getUser().getUsername() : "Неизвестный автор",
                recipe.getComments() != null ? recipe.getComments().size() : 0,
                recipe.isPublished(),
                recipe.getCreatedAt(),
                recipe.getUpdatedAt()
        );
    }

    public RecipeDetailsDto toDetailsDto(Recipe recipe) {
        if (recipe == null) return null;

        List<InventoryDto> inventoryDtos = recipe.getInventoryItems() != null
                ? recipe.getInventoryItems().stream()
                .map(inventoryConverter::toDto)
                .collect(Collectors.toList())
                : new ArrayList<>();

        List<CommentDto> commentDtos = recipe.getComments() != null
                ? recipe.getComments().stream()
                .map(commentConverter::toDto)
                .collect(Collectors.toList())
                : new ArrayList<>();

        return new RecipeDetailsDto(
                recipe.getId(),
                recipe.getTitle(),
                categoryConverter.toDto(recipe.getCategory()),
                authorConverter.toDto(recipe.getAuthor()),
                inventoryDtos,
                recipe.getIngredients() != null ? recipe.getIngredients() : new ArrayList<>(),
                recipe.getDescription(),
                commentDtos,
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

    public List<RecipeDetailsDto> toDetailsDtoList(List<Recipe> recipes) {
        return recipes.stream()
                .map(this::toDetailsDto)
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

    public RecipeSummaryDto toSummaryDtoWithCommentCount(Recipe recipe) {
        if (recipe == null) return null;

        int commentCount = commentService.getCommentCountForRecipe(recipe.getId());

        return new RecipeSummaryDto(
                recipe.getId(),
                recipe.getTitle(),
                recipe.getCategory() != null ? recipe.getCategory().getName() : "Без категории",
                recipe.getAuthor() != null && recipe.getAuthor().getUser() != null
                        ? recipe.getAuthor().getUser().getUsername() : "Неизвестный автор",
                commentCount, // реальное количество из сервиса
                recipe.isPublished(),
                recipe.getCreatedAt(),
                recipe.getUpdatedAt()
        );
    }

}