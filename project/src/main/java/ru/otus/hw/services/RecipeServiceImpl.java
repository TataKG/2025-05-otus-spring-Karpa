package ru.otus.hw.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.converters.RecipeConverter;
import ru.otus.hw.dto.RecipeDto;
import ru.otus.hw.dto.RecipeSummaryDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Category;
import ru.otus.hw.models.Inventory;
import ru.otus.hw.models.Recipe;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.CategoryRepository;
import ru.otus.hw.repositories.InventoryRepository;
import ru.otus.hw.repositories.RecipeRepository;
import ru.otus.hw.util.MessageProvider;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Transactional
public class RecipeServiceImpl implements RecipeService {

    private final RecipeRepository recipeRepository;
    private final CategoryRepository categoryRepository;
    private final AuthorRepository authorRepository;
    private final InventoryRepository inventoryRepository;
    private final RecipeConverter recipeConverter;
    private final MessageProvider messageProvider;

    public RecipeServiceImpl(RecipeRepository recipeRepository,
                             CategoryRepository categoryRepository,
                             AuthorRepository authorRepository,
                             InventoryRepository inventoryRepository,
                             RecipeConverter recipeConverter,
                             MessageProvider messageProvider) {
        this.recipeRepository = recipeRepository;
        this.categoryRepository = categoryRepository;
        this.authorRepository = authorRepository;
        this.inventoryRepository = inventoryRepository;
        this.recipeConverter = recipeConverter;
        this.messageProvider = messageProvider;
    }

    @Override
    public RecipeDto createRecipe(String title, Long categoryId, Long authorId,
                                  List<String> ingredients, String description) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("category.not_found", categoryId)
                ));

        Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("author.not_found", authorId)
                ));

        Recipe recipe = new Recipe(title, category, author, ingredients, description);
        Recipe savedRecipe = recipeRepository.save(recipe);
        return recipeConverter.toDto(savedRecipe);
    }

    @Override
    public Optional<RecipeDto> getRecipeById(Long id) {
        return recipeRepository.findByIdWithDetails(id)
                .map(recipeConverter::toDto);
    }

    @Override
    public Optional<RecipeDto> getRecipeByIdWithAllRelations(Long id) {
        return recipeRepository.findByIdWithAllRelations(id)
                .map(recipeConverter::toDto);
    }

    @Override
    public List<RecipeSummaryDto> getAllRecipes() {
        return recipeRepository.findAllWithAssociations().stream()
                .map(recipeConverter::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecipeSummaryDto> getRecipesByCategory(Long categoryId) {
        return recipeRepository.findByCategoryIdWithDetails(categoryId).stream()
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
    public List<RecipeSummaryDto> searchRecipesByTitle(String title) {
        return recipeRepository.findByTitleContainingIgnoreCase(title).stream()
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
    public List<RecipeSummaryDto> findRecipesByFilters(String title, Long categoryId, Long authorId) {
        return recipeRepository.findByFilters(title, categoryId, authorId).stream()
                .map(recipeConverter::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    public RecipeDto addInventoryToRecipe(Long recipeId, List<Long> inventoryIds) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("recipe.not_found", recipeId)
                ));

        List<Inventory> inventoryItems = StreamSupport.stream(
                        inventoryRepository.findAllById(inventoryIds).spliterator(), false)
                .collect(Collectors.toList());

        for (Inventory inventory : inventoryItems) {
            recipe.addInventoryItem(inventory);
        }

        Recipe updatedRecipe = recipeRepository.save(recipe);
        return recipeConverter.toDto(updatedRecipe);
    }
}