package ru.otus.hw.services;

import ru.otus.hw.dto.InventoryDto;
import ru.otus.hw.dto.RecipeDto;
import ru.otus.hw.dto.RecipeSummaryDto;

import java.util.List;
import java.util.Optional;

public interface RecipeService {

    RecipeDto createRecipe(String title, Long categoryId, Long authorId,
                           List<String> ingredients, String description, boolean published);

    RecipeDto createRecipeWithInventory(String title, Long categoryId, Long authorId,
                                        List<String> ingredients, String description,
                                        List<Long> inventoryIds, boolean published);

    RecipeDto updateRecipe(Long id, String title, Long categoryId, Long authorId, List<String> ingredients,
                           String description, List<Long> inventoryIds, boolean published);

    Optional<RecipeDto> getRecipeById(Long id);

    Optional<RecipeDto> getRecipeByIdWithBasicRelations(Long id);

    Optional<RecipeDto> getRecipeByIdWithAllRelations(Long id);

    RecipeDto getRecipeWithDetails(Long id);

    List<RecipeSummaryDto> getAllPublishedRecipes();

    List<RecipeSummaryDto> getRecipesByAuthor(Long authorId);

    List<RecipeSummaryDto> getPublishedRecipesByAuthor(Long authorId);

    List<RecipeSummaryDto> searchPublishedRecipesByTitle(String title);

    List<RecipeSummaryDto> searchPublishedRecipesByIngredient(String ingredient);

    List<RecipeSummaryDto> findPublishedRecipesByFilters(String title, Long categoryId, Long authorId);

    List<RecipeDto> findPublishedRecipesWithFilters(String search, Long categoryId, Long authorId);

    List<RecipeSummaryDto> getRecentPublishedRecipes(int limit);

    List<RecipeSummaryDto> getPopularPublishedRecipes(int limit);

    void deleteRecipe(Long id);

    RecipeDto publishRecipe(Long id);

    RecipeDto unpublishRecipe(Long id);

    long getPublishedRecipesCount();

}