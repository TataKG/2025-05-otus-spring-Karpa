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
        RecipeDto recipe = getRecipeByIdWithBasicRelations(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("recipe.not_found", id)
                ));

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

        if (inventoryIds != null && !inventoryIds.isEmpty()) {
            List<Inventory> inventoryItems = getInventoryItemsByIds(inventoryIds);
            recipe.getInventoryItems().addAll(inventoryItems);

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
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("recipe.not_found", id)
                ));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("category.not_found", categoryId)
                ));

        recipe.setTitle(title);
        recipe.setCategory(category);
        recipe.setDescription(description);
        recipe.setPublished(published);

        recipe.getIngredients().clear();
        if (ingredients != null && !ingredients.isEmpty()) {
            recipe.getIngredients().addAll(ingredients);
        }

        recipe.getInventoryItems().clear();
        if (inventoryIds != null && !inventoryIds.isEmpty()) {
            List<Inventory> inventoryItems = getInventoryItemsByIds(inventoryIds);
            recipe.getInventoryItems().addAll(inventoryItems);

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
        return Optional.of(getRecipeWithDetails(id))
                .map(recipeConverter::toDtoFromDetails);
    }

    @Override
    public List<RecipeSummaryDto> getAllPublishedRecipes() {
        return recipeRepository.findPublishedRecipesWithBasicAssociations().stream()
                .map(recipeConverter::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecipeSummaryDto> getRecipesByAuthor(Long authorId) {
        return recipeRepository.findByAuthorIdWithDetails(authorId).stream()
                .map(recipeConverter::toSummaryDtoWithCommentCount)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecipeSummaryDto> getPublishedRecipesByAuthor(Long authorId) {
        return recipeRepository.findPublishedByAuthorIdWithDetails(authorId).stream()
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
    public List<RecipeSummaryDto> searchPublishedRecipesByIngredient(String ingredient) {
        return recipeRepository.findPublishedByIngredientContaining(ingredient).stream()
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
    public List<RecipeDto> findPublishedRecipesWithFilters(String search, Long categoryId, Long authorId) {
        List<Recipe> recipes;

        if (search != null && !search.trim().isEmpty()) {
            recipes = recipeRepository.findPublishedByTitleContainingIgnoreCase(search.trim());
        } else if (categoryId != null || authorId != null) {
            recipes = recipeRepository.findPublishedByFilters(null, categoryId, authorId);
        } else {
            recipes = recipeRepository.findPublishedRecipesWithBasicAssociations();
        }

        return recipes.stream()
                .map(recipeConverter::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteRecipe(Long id) {
        if (!recipeRepository.existsById(id)) {
            throw new EntityNotFoundException(messageProvider.getMessage("recipe.not_found", id));
        }

        commentRepository.deleteByRecipeId(id);
        recipeRepository.deleteInventoryAssociations(id);
        recipeRepository.deleteIngredients(id);
        recipeRepository.deleteById(id);
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
    public List<InventoryDto> getInventoryByRecipeId(Long recipeId) {
        List<Inventory> inventory = inventoryRepository.findByRecipeId(recipeId);
        return inventory.stream()
                .map(inventoryConverter::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isInventoryUsedInRecipes(Long inventoryId) {
        return inventoryRepository.isUsedInRecipes(inventoryId);
    }

    @Override
    public long getRecipeCountByInventoryId(Long inventoryId) {
        return inventoryRepository.countPublishedRecipesByInventoryId(inventoryId);
    }

    private List<Inventory> getInventoryItemsByIds(List<Long> inventoryIds) {
        return StreamSupport
                .stream(inventoryRepository.findAllById(inventoryIds).spliterator(), false)
                .collect(Collectors.toList());
    }
}