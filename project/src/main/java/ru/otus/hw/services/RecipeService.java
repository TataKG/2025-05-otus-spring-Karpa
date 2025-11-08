package ru.otus.hw.services;

import ru.otus.hw.dto.InventoryDto;
import ru.otus.hw.dto.RecipeDto;
import ru.otus.hw.dto.RecipeSummaryDto;
import ru.otus.hw.dto.RecipeWithDetailsDto;

import java.util.List;
import java.util.Optional;

public interface RecipeService {

        RecipeDto createRecipe(String title, Long categoryId, Long authorId,
                               List<String> ingredients, String description, boolean published);

        RecipeDto updateRecipe(Long id, String title, Long categoryId, List<String> ingredients,
                               String description, List<Long> inventoryIds, boolean published);

        Optional<RecipeDto> getRecipeById(Long id);
        Optional<RecipeDto> getRecipeByIdWithAllRelations(Long id);

        // Новый метод для получения рецепта с деталями (без MultipleBagFetchException)
        RecipeWithDetailsDto getRecipeWithDetails(Long id);

        // Новый метод для получения рецепта с базовыми связями
        Optional<RecipeDto> getRecipeByIdWithBasicRelations(Long id);

        List<RecipeSummaryDto> getAllRecipes();
        List<RecipeSummaryDto> getAllPublishedRecipes();
        List<RecipeSummaryDto> getPublishedRecipes();
        List<RecipeSummaryDto> getRecipesByAuthor(Long authorId);
        List<RecipeSummaryDto> getPublishedRecipesByAuthor(Long authorId);
        List<RecipeSummaryDto> getPublishedRecipesByCategory(Long categoryId);

        List<RecipeSummaryDto> searchRecipesByTitle(String title);
        List<RecipeSummaryDto> searchPublishedRecipesByTitle(String title);
        List<RecipeSummaryDto> searchRecipesByIngredient(String ingredient);
        List<RecipeSummaryDto> searchPublishedRecipesByIngredient(String ingredient);

        List<RecipeSummaryDto> findRecipesByFilters(String title, Long categoryId, Long authorId);
        List<RecipeSummaryDto> findPublishedRecipesByFilters(String title, Long categoryId, Long authorId);

        RecipeDto addInventoryToRecipe(Long recipeId, List<Long> inventoryIds);
        RecipeDto removeInventoryFromRecipe(Long recipeId, List<Long> inventoryIds);
        void deleteRecipe(Long id);

        RecipeDto publishRecipe(Long id);
        RecipeDto unpublishRecipe(Long id);

        List<RecipeSummaryDto> getRecentPublishedRecipes(int limit);
        List<RecipeSummaryDto> getPopularPublishedRecipes(int limit);

        List<RecipeDto> findPublishedRecipesWithFilters(String search, Long categoryId, Long authorId);

        long getPublishedRecipesCount();
        long getTotalRecipesCount();
        long getRecipesCountByAuthor(Long authorId);
        long getPublishedRecipesCountByAuthor(Long authorId);

        List<InventoryDto> getInventoryByRecipeId(Long recipeId);
        List<InventoryDto> getInventoryByRecipeIds(List<Long> recipeIds);
        boolean isInventoryUsedInRecipes(Long inventoryId);
        boolean isInventoryUsedInPublishedRecipes(Long inventoryId);
        long getRecipeCountByInventoryId(Long inventoryId);
        RecipeDto createRecipeWithInventory(String title, Long categoryId, Long authorId,
                                            List<String> ingredients, String description,
                                            List<Long> inventoryIds, boolean published);

}