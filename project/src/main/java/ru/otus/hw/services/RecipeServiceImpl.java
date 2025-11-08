package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.converters.InventoryConverter;
import ru.otus.hw.converters.RecipeConverter;
import ru.otus.hw.dto.*;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Category;
import ru.otus.hw.models.Inventory;
import ru.otus.hw.models.Recipe;
import ru.otus.hw.repositories.*;
import ru.otus.hw.util.MessageProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Transactional
@RequiredArgsConstructor
public class RecipeServiceImpl implements RecipeService {

    private final RecipeRepository recipeRepository;
    private final CategoryRepository categoryRepository;
    private final AuthorRepository authorRepository;
    private final InventoryRepository inventoryRepository;
    private final RecipeConverter recipeConverter;
    private final MessageProvider messageProvider;
    private final CommentRepository commentRepository;
    private final InventoryService inventoryService;
    private final CommentService commentService;
    private final InventoryConverter inventoryConverter;

    @Override
    public RecipeWithDetailsDto getRecipeWithDetails(Long id) {
        // Основные данные рецепта без коллекций
        RecipeDto recipe = getRecipeByIdWithBasicRelations(id)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found"));

        // Отдельные запросы для коллекций
        List<InventoryDto> inventory = inventoryService.getInventoryByRecipeId(id);
        List<CommentDto> comments = commentService.getCommentsByRecipeId(id);
        int commentCount = commentService.getCommentCountForRecipe(id);

        return new RecipeWithDetailsDto(recipe, inventory, comments, commentCount);
    }

    @Override
    public Optional<RecipeDto> getRecipeByIdWithBasicRelations(Long id) {
        return recipeRepository.findByIdWithBasicRelations(id)
                .map(recipeConverter::toDto);
    }

    @Override
    public RecipeDto createRecipe(String title, Long categoryId, Long authorId,
                                  List<String> ingredients, String description, boolean published) {
        return createRecipeWithInventory(title, categoryId, authorId, ingredients, description, null, published);
    }

    @Override
    public RecipeDto createRecipeWithInventory(String title, Long categoryId, Long authorId,
                                               List<String> ingredients, String description,
                                               List<Long> inventoryIds, boolean published) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("category.not_found", categoryId)
                ));

        Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("author.not_found", authorId)
                ));

        Recipe recipe = new Recipe(title, category, author, ingredients, description);
        recipe.setPublished(published);

        // Добавляем инвентарь при создании
        if (inventoryIds != null && !inventoryIds.isEmpty()) {
            Iterable<Inventory> inventoryIterable = inventoryRepository.findAllById(inventoryIds);
            List<Inventory> inventoryItems = new ArrayList<>();
            inventoryIterable.forEach(inventoryItems::add);

            recipe.getInventoryItems().addAll(inventoryItems);

            // Двусторонняя связь
            for (Inventory inventory : inventoryItems) {
                if (!inventory.getRecipes().contains(recipe)) {
                    inventory.getRecipes().add(recipe);
                }
            }
        }

        Recipe savedRecipe = recipeRepository.save(recipe);
        return recipeConverter.toDto(savedRecipe);
    }

    @Override
    public RecipeDto updateRecipe(Long id, String title, Long categoryId,
                                  List<String> ingredients, String description,
                                  List<Long> inventoryIds, boolean published) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found"));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        recipe.setTitle(title);
        recipe.setCategory(category);
        recipe.setDescription(description);
        recipe.setPublished(published);

        // Обновляем ингредиенты
        recipe.getIngredients().clear();
        if (ingredients != null && !ingredients.isEmpty()) {
            recipe.getIngredients().addAll(ingredients);
        }

        // Обновляем инвентарь - ИСПРАВЛЕННАЯ ЛОГИКА
        recipe.getInventoryItems().clear();
        if (inventoryIds != null && !inventoryIds.isEmpty()) {
            Iterable<Inventory> inventoryIterable = inventoryRepository.findAllById(inventoryIds);
            List<Inventory> inventoryItems = new ArrayList<>();
            inventoryIterable.forEach(inventoryItems::add);

            recipe.getInventoryItems().addAll(inventoryItems);

            // Двусторонняя связь
            for (Inventory inventory : inventoryItems) {
                if (!inventory.getRecipes().contains(recipe)) {
                    inventory.getRecipes().add(recipe);
                }
            }
        }

        Recipe savedRecipe = recipeRepository.save(recipe);
        return recipeConverter.toDto(savedRecipe);
    }

    @Override
    public Optional<RecipeDto> getRecipeById(Long id) {
        return recipeRepository.findByIdWithBasicRelations(id)
                .map(recipeConverter::toDto);
    }

    @Override
    public Optional<RecipeDto> getRecipeByIdWithAllRelations(Long id) {
        // Используем новый метод с отдельными запросами
        return Optional.of(getRecipeWithDetails(id))
                .map(recipeConverter::toDtoFromDetails);
    }

    @Override
    public List<RecipeSummaryDto> getAllRecipes() {
        return recipeRepository.findAllWithBasicAssociations().stream()
                .map(recipeConverter::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecipeSummaryDto> getAllPublishedRecipes() {
        return recipeRepository.findPublishedRecipesWithBasicAssociations().stream()
                .map(recipeConverter::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecipeSummaryDto> getPublishedRecipes() {
        return recipeRepository.findByPublishedTrueWithBasicAssociations().stream()
                .map(recipeConverter::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecipeSummaryDto> getRecipesByAuthor(Long authorId) {
        return recipeRepository.findByAuthorIdWithDetails(authorId).stream()
                .map(recipeConverter::toSummaryDtoWithCommentCount) // используем новый метод
                .collect(Collectors.toList());
    }

    @Override
    public List<RecipeSummaryDto> getPublishedRecipesByAuthor(Long authorId) {
        return recipeRepository.findPublishedByAuthorIdWithDetails(authorId).stream()
                .map(recipeConverter::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecipeSummaryDto> getPublishedRecipesByCategory(Long categoryId) {
        return recipeRepository.findPublishedByCategoryIdWithBasicDetails(categoryId).stream()
                .map(recipeConverter::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecipeSummaryDto> searchRecipesByTitle(String title) {
        return recipeRepository.findByTitleContainingIgnoreCase(title).stream()
                .map(recipeConverter::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecipeSummaryDto> searchPublishedRecipesByTitle(String title) {
        return recipeRepository.findPublishedByTitleContainingIgnoreCase(title).stream()
                .map(recipeConverter::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecipeSummaryDto> searchRecipesByIngredient(String ingredient) {
        return recipeRepository.findByIngredientContaining(ingredient).stream()
                .map(recipeConverter::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecipeSummaryDto> searchPublishedRecipesByIngredient(String ingredient) {
        return recipeRepository.findPublishedByIngredientContaining(ingredient).stream()
                .map(recipeConverter::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecipeSummaryDto> findRecipesByFilters(String title, Long categoryId, Long authorId) {
        return recipeRepository.findByFilters(title, categoryId, authorId).stream()
                .map(recipeConverter::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecipeSummaryDto> findPublishedRecipesByFilters(String title, Long categoryId, Long authorId) {
        return recipeRepository.findPublishedByFilters(title, categoryId, authorId).stream()
                .map(recipeConverter::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    public RecipeDto addInventoryToRecipe(Long recipeId, List<Long> inventoryIds) {
        Recipe recipe = recipeRepository.findByIdWithBasicRelations(recipeId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("recipe.not_found", recipeId)
                ));

        List<Inventory> inventoryItems = getInventoryItemsByIds(inventoryIds);

        for (Inventory inventory : inventoryItems) {
            if (!recipe.getInventoryItems().contains(inventory)) {
                recipe.addInventoryItem(inventory);
            }
        }

        Recipe updatedRecipe = recipeRepository.save(recipe);
        return recipeConverter.toDto(updatedRecipe);
    }

    @Override
    public RecipeDto removeInventoryFromRecipe(Long recipeId, List<Long> inventoryIds) {
        Recipe recipe = recipeRepository.findByIdWithBasicRelations(recipeId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("recipe.not_found", recipeId)
                ));

        List<Inventory> inventoryItems = getInventoryItemsByIds(inventoryIds);

        for (Inventory inventory : inventoryItems) {
            if (recipe.getInventoryItems().contains(inventory)) {
                recipe.removeInventoryItem(inventory);
            }
        }

        Recipe updatedRecipe = recipeRepository.save(recipe);
        return recipeConverter.toDto(updatedRecipe);
    }

    @Override
    @Transactional
    public void deleteRecipe(Long id) {
        try {
            System.out.println("Starting deletion of recipe: " + id);

            // Проверяем существование рецепта
            if (!recipeRepository.existsById(id)) {
                throw new EntityNotFoundException("Recipe not found: " + id);
            }

            // 1. Удаляем комментарии
            System.out.println("Deleting comments for recipe: " + id);
            commentRepository.deleteByRecipeId(id);

            // 2. Удаляем связи с инвентарем
            System.out.println("Deleting inventory associations...");
            recipeRepository.deleteInventoryAssociations(id);

            // 3. Удаляем ингредиенты
            System.out.println("Deleting ingredients...");
            recipeRepository.deleteIngredients(id);

            // 4. Удаляем сам рецепт
            System.out.println("Deleting recipe...");
            recipeRepository.deleteById(id);

            System.out.println("Recipe deleted successfully: " + id);

        } catch (Exception e) {
            System.err.println("Error in deleteRecipe: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public RecipeDto publishRecipe(Long id) {
        Recipe recipe = recipeRepository.findByIdWithBasicRelations(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("recipe.not_found", id)
                ));

        recipe.publish();
        Recipe publishedRecipe = recipeRepository.save(recipe);
        return recipeConverter.toDto(publishedRecipe);
    }

    @Override
    public RecipeDto unpublishRecipe(Long id) {
        Recipe recipe = recipeRepository.findByIdWithBasicRelations(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("recipe.not_found", id)
                ));

        recipe.unpublish();
        Recipe unpublishedRecipe = recipeRepository.save(recipe);
        return recipeConverter.toDto(unpublishedRecipe);
    }

    @Override
    public List<RecipeSummaryDto> getRecentPublishedRecipes(int limit) {
        return recipeRepository.findRecentPublishedRecipes(limit).stream()
                .map(recipeConverter::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecipeSummaryDto> getPopularPublishedRecipes(int limit) {
        return recipeRepository.findPopularPublishedRecipes(limit).stream()
                .map(recipeConverter::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    public long getPublishedRecipesCount() {
        return recipeRepository.countByPublishedTrue();
    }

    @Override
    public long getTotalRecipesCount() {
        return recipeRepository.count();
    }

    @Override
    public long getRecipesCountByAuthor(Long authorId) {
        return recipeRepository.countByAuthorId(authorId);
    }

    @Override
    public long getPublishedRecipesCountByAuthor(Long authorId) {
        return recipeRepository.countByAuthorIdAndPublishedTrue(authorId);
    }

    @Override
    public List<RecipeDto> findPublishedRecipesWithFilters(String search, Long categoryId, Long authorId) {
        List<Recipe> recipes;

        if (search != null && !search.trim().isEmpty()) {
            // Поиск по названию
            recipes = recipeRepository.findPublishedByTitleContainingIgnoreCase(search.trim());
        } else if (categoryId != null || authorId != null) {
            // Фильтрация по категории и/или автору
            recipes = recipeRepository.findPublishedByFilters(
                    null, // search
                    categoryId,
                    authorId
            );
        } else {
            // Все опубликованные рецепты
            recipes = recipeRepository.findPublishedRecipesWithBasicAssociations();
        }

        return recipes.stream()
                .map(recipeConverter::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryDto> getInventoryByRecipeId(Long recipeId) {
        List<Inventory> inventory = inventoryRepository.findByRecipeId(recipeId);
        return inventory.stream()
                .map(inventoryConverter::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryDto> getInventoryByRecipeIds(List<Long> recipeIds) {
        List<Inventory> inventory = inventoryRepository.findByRecipeIds(recipeIds);
        return inventory.stream()
                .map(inventoryConverter::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isInventoryUsedInRecipes(Long inventoryId) {
        return inventoryRepository.isUsedInRecipes(inventoryId);
    }

    @Override
    public boolean isInventoryUsedInPublishedRecipes(Long inventoryId) {
        return inventoryRepository.isUsedInPublishedRecipes(inventoryId);
    }

    @Override
    public long getRecipeCountByInventoryId(Long inventoryId) {
        return inventoryRepository.countPublishedRecipesByInventoryId(inventoryId);
    }

    // Вспомогательный метод для получения инвентаря по IDs с проверкой
    private List<Inventory> getInventoryItemsByIds(List<Long> inventoryIds) {
        return StreamSupport
                .stream(inventoryRepository.findAllById(inventoryIds).spliterator(), false)
                .collect(Collectors.toList());
    }


}