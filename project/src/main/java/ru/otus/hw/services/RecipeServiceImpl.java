package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.converters.RecipeConverter;
import ru.otus.hw.dto.*;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.*;
import ru.otus.hw.repositories.*;
import ru.otus.hw.util.MessageProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Override
    @Transactional(readOnly = true)
    public RecipeDto getRecipeWithDetails(Long id) {
        Recipe recipe = recipeRepository.findByIdWithAllRelations(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("recipe.not_found", id)
                ));

        List<Comment> comments = commentRepository.findByRecipeIdWithUser(id);
        recipe.getComments().clear();
        recipe.getComments().addAll(comments);
        comments.forEach(comment -> comment.setRecipe(recipe));

        return recipeConverter.toDto(recipe);
    }

    @Override
    public Optional<RecipeDto> getRecipeById(Long id) {
        return getRecipeByIdWithBasicRelations(id);
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

        validateRecipeData(title, categoryId, authorId, ingredients, description, true);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("category.not_found", categoryId)
                ));

        Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("author.not_found", authorId)
                ));

        Recipe recipe = new Recipe(title.trim(), category, author, description.trim());

        List<String> validIngredients = ingredients.stream()
                .filter(ingredient -> ingredient != null && !ingredient.trim().isEmpty())
                .toList();

        if (validIngredients.isEmpty()) {
            throw new IllegalArgumentException(messageProvider.getMessage("recipe.ingredients.empty"));
        }

        recipe.getIngredients().addAll(validIngredients);
        recipe.setPublished(published);

        if (inventoryIds != null && !inventoryIds.isEmpty()) {
            List<Long> validInventoryIds = inventoryIds.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!validInventoryIds.isEmpty()) {
                List<Inventory> inventoryItems = new ArrayList<>();
                inventoryRepository.findAllById(validInventoryIds).forEach(inventoryItems::add);
                recipe.getInventoryItems().addAll(inventoryItems);
            }
        }

        Recipe savedRecipe = recipeRepository.save(recipe);
        return recipeConverter.toDto(savedRecipe);
    }

    @Override
    @Transactional
    public RecipeDto updateRecipe(Long id, String title, Long categoryId, Long authorId,
                                  List<String> ingredients, String description,
                                  List<Long> inventoryIds, boolean published) {
        try {
            validateRecipeData(title, categoryId, null, ingredients, description, false);

            Recipe recipe = recipeRepository.findByIdWithBasicRelations(id)
                    .orElseThrow(() -> new EntityNotFoundException(
                            messageProvider.getMessage("recipe.not_found", id)
                    ));

            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            messageProvider.getMessage("category.not_found", categoryId)
                    ));

            recipe.setTitle(title.trim());
            recipe.setCategory(category);
            recipe.setDescription(description.trim());
            recipe.setPublished(published);

            recipeRepository.deleteIngredients(id);

            if (ingredients != null && !ingredients.isEmpty()) {
                for (int i = 0; i < ingredients.size(); i++) {
                    String ingredient = ingredients.get(i);
                    if (ingredient != null && !ingredient.trim().isEmpty()) {
                        recipeRepository.addIngredient(id, ingredient.trim(), i);
                    }
                }
            }

            recipe.getInventoryItems().clear();
            if (inventoryIds != null && !inventoryIds.isEmpty()) {
                List<Long> validInventoryIds = inventoryIds.stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                if (!validInventoryIds.isEmpty()) {
                    Iterable<Inventory> inventoryIterable = inventoryRepository.findAllById(validInventoryIds);
                    List<Inventory> inventoryItems = new ArrayList<>();
                    inventoryIterable.forEach(inventoryItems::add);
                    recipe.getInventoryItems().addAll(inventoryItems);
                }
            }

            Recipe savedRecipe = recipeRepository.save(recipe);
            return recipeConverter.toDto(savedRecipe);
        } catch (Exception e) {
            System.err.println("Error updating recipe: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Optional<RecipeDto> getRecipeByIdWithAllRelations(Long id) {
        return recipeRepository.findByIdWithAllRelations(id)
                .map(recipeConverter::toDto);
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
                .map(recipeConverter::toSummaryDto)
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

    private void validateRecipeData(String title, Long categoryId, Long authorId,
                                    List<String> ingredients, String description,
                                    boolean isCreateOperation) {

        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException(messageProvider.getMessage("recipe.title.required"));
        }
        if (title.trim().length() < 2) {
            throw new IllegalArgumentException(messageProvider.getMessage("recipe.title.min_length"));
        }
        if (categoryId == null) {
            throw new IllegalArgumentException(messageProvider.getMessage("recipe.category.required"));
        }
        if (isCreateOperation && authorId == null) {
            throw new IllegalArgumentException(messageProvider.getMessage("author.id.null"));
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException(messageProvider.getMessage("recipe.description.required"));
        }
        if (description.trim().length() < 10) {
            throw new IllegalArgumentException(messageProvider.getMessage("recipe.description.min_length"));
        }
        if (ingredients == null || ingredients.isEmpty()) {
            throw new IllegalArgumentException(messageProvider.getMessage("recipe.ingredients.required"));
        }

        boolean hasValidIngredients = ingredients.stream()
                .anyMatch(ingredient -> ingredient != null && !ingredient.trim().isEmpty());
        if (!hasValidIngredients) {
            throw new IllegalArgumentException(messageProvider.getMessage("recipe.ingredients.min_one"));
        }
    }
}