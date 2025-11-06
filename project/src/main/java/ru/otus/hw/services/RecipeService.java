package ru.otus.hw.services;

import ru.otus.hw.dto.RecipeDto;
import ru.otus.hw.dto.RecipeSummaryDto;

import java.util.List;
import java.util.Optional;

public interface RecipeService {
    RecipeDto createRecipe(String title, Long categoryId, Long authorId,
                           List<String> ingredients, String description);

    Optional<RecipeDto> getRecipeById(Long id);

    Optional<RecipeDto> getRecipeByIdWithAllRelations(Long id);

    List<RecipeSummaryDto> getAllRecipes();

    List<RecipeSummaryDto> getRecipesByCategory(Long categoryId);

    List<RecipeSummaryDto> getRecipesByAuthor(Long authorId);

    List<RecipeSummaryDto> searchRecipesByTitle(String title);

    List<RecipeSummaryDto> searchRecipesByIngredient(String ingredient);

    List<RecipeSummaryDto> findRecipesByFilters(String title, Long categoryId, Long authorId);

    RecipeDto addInventoryToRecipe(Long recipeId, List<Long> inventoryIds);
}
