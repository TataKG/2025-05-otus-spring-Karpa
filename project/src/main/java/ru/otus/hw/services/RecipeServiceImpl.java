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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class RecipeServiceImpl implements RecipeService {
    private final RecipeRepository recipeRepository;
    private final RecipeConverter recipeConverter;
    private final MessageProvider messageProvider;
    private final CommentRepository commentRepository;
    private final CommentService commentService;

    private final CategoryService categoryService;
    private final AuthorService authorService;
    private final InventoryService inventoryService;

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
        try {
            System.out.println("=== CREATING RECIPE ===");
            System.out.println("Title: " + title);
            System.out.println("Category ID: " + categoryId);
            System.out.println("Author ID: " + authorId);
            System.out.println("Ingredients: " + ingredients);
            System.out.println("Description length: " + (description != null ? description.length() : "null"));
            System.out.println("Inventory IDs: " + inventoryIds);
            System.out.println("Published: " + published);

            // ВАЖНО: Проверка и нормализация inventoryIds
            List<Long> processedInventoryIds = (inventoryIds != null) ?
                    inventoryIds.stream()
                            .filter(id -> id != null)
                            .collect(Collectors.toList()) :
                    new ArrayList<>();

            System.out.println("Processed inventory IDs: " + processedInventoryIds);
            System.out.println("Processed inventory IDs size: " + processedInventoryIds.size());

            // Проверка входных данных
            if (title == null || title.trim().isEmpty()) {
                throw new IllegalArgumentException("Название рецепта не может быть пустым");
            }
            if (categoryId == null) {
                throw new IllegalArgumentException("ID категории не может быть null");
            }
            if (authorId == null) {
                throw new IllegalArgumentException("ID автора не может быть null");
            }
            if (ingredients == null || ingredients.isEmpty()) {
                throw new IllegalArgumentException("Список ингредиентов не может быть пустым");
            }
            if (description == null || description.trim().isEmpty()) {
                throw new IllegalArgumentException("Описание рецепта не может быть пустым");
            }

            // Проверка категории
            CategoryDto categoryDto = categoryService.getCategoryEntityForInternalUse(categoryId)
                    .orElseThrow(() -> {
                        System.err.println("Category not found: " + categoryId);
                        return new EntityNotFoundException(
                                messageProvider.getMessage("category.not_found", categoryId)
                        );
                    });
            System.out.println("Category found: " + categoryDto.name());

            // Проверка автора
            AuthorDto authorDto = authorService.getAuthorForInternalUse(authorId)
                    .orElseThrow(() -> {
                        System.err.println("Author not found: " + authorId);
                        return new EntityNotFoundException(
                                messageProvider.getMessage("author.not_found", authorId)
                        );
                    });
            System.out.println("Author found: " + authorDto.user().username());

            Category category = createCategoryEntityFromDto(categoryDto);
            Author author = createAuthorEntityFromDto(authorDto);

            // Создаем рецепт
            Recipe recipe = new Recipe(title, category, author, description);

            // Добавляем ингредиенты
            if (ingredients != null) {
                // Фильтруем пустые ингредиенты
                List<String> validIngredients = ingredients.stream()
                        .filter(ingredient -> ingredient != null && !ingredient.trim().isEmpty())
                        .collect(Collectors.toList());

                if (validIngredients.isEmpty()) {
                    throw new IllegalArgumentException("Добавьте хотя бы один непустой ингредиент");
                }

                recipe.getIngredients().addAll(validIngredients);
                System.out.println("Added " + validIngredients.size() + " valid ingredients");
            }

            recipe.setPublished(published);

            // Обрабатываем инвентарь с улучшенной обработкой ошибок
            if (processedInventoryIds != null && !processedInventoryIds.isEmpty()) {
                System.out.println("Processing inventory items: " + processedInventoryIds);

                try {
                    List<InventoryDto> inventoryDtos = inventoryService.getInventoryByIdsForInternalUse(processedInventoryIds);
                    System.out.println("Found " + inventoryDtos.size() + " inventory items");

                    if (inventoryDtos.size() != processedInventoryIds.size()) {
                        System.out.println("Warning: Some inventory items were not found. Expected: " +
                                processedInventoryIds.size() + ", Found: " + inventoryDtos.size());

                        // Найдем какие ID не были найдены
                        List<Long> foundIds = inventoryDtos.stream()
                                .map(InventoryDto::id)
                                .collect(Collectors.toList());

                        List<Long> missingIds = processedInventoryIds.stream()
                                .filter(id -> !foundIds.contains(id))
                                .collect(Collectors.toList());

                        System.out.println("Missing inventory IDs: " + missingIds);
                    }

                    int addedCount = 0;
                    for (InventoryDto inventoryDto : inventoryDtos) {
                        if (inventoryDto != null) {
                            try {
                                Inventory inventory = createInventoryEntityFromDto(inventoryDto);
                                recipe.addInventoryItem(inventory);
                                addedCount++;
                                System.out.println("Added inventory: " + inventory.getName() + " (ID: " + inventory.getId() + ")");
                            } catch (Exception e) {
                                System.err.println("Error adding inventory item " + inventoryDto.id() + ": " + e.getMessage());
                                // Продолжаем с следующими элементами
                            }
                        }
                    }
                    System.out.println("Successfully added " + addedCount + " inventory items");

                } catch (Exception e) {
                    System.err.println("Error processing inventory items: " + e.getMessage());
                    System.err.println("Continuing without inventory items");
                    // Продолжаем создание рецепта без инвентаря
                    // Можно выбросить исключение, если инвентарь обязателен:
                    // throw new IllegalArgumentException("Ошибка при обработке инвентаря: " + e.getMessage());
                }
            } else {
                System.out.println("No inventory items to add");
            }

            System.out.println("Saving recipe to database...");
            System.out.println("Recipe details before save:");
            System.out.println("  - Title: " + recipe.getTitle());
            System.out.println("  - Category: " + (recipe.getCategory() != null ? recipe.getCategory().getName() : "null"));
            System.out.println("  - Author: " + (recipe.getAuthor() != null ? recipe.getAuthor().getUser().getUsername() : "null"));
            System.out.println("  - Ingredients count: " + recipe.getIngredients().size());
            System.out.println("  - Inventory items count: " + recipe.getInventoryItems().size());
            System.out.println("  - Published: " + recipe.isPublished());

            Recipe savedRecipe = recipeRepository.save(recipe);
            System.out.println("Recipe saved with ID: " + savedRecipe.getId());

            System.out.println("Converting recipe to DTO...");
            RecipeDto result = recipeConverter.toDto(savedRecipe);
            System.out.println("Recipe conversion completed");
            System.out.println("Final recipe DTO ID: " + result.id());

            return result;

        } catch (IllegalArgumentException | EntityNotFoundException e) {
            System.err.println("Validation error in createRecipeWithInventory: " + e.getMessage());
            e.printStackTrace();
            throw e; // Перебрасываем проверенные исключения
        } catch (Exception e) {
            System.err.println("Unexpected error in createRecipeWithInventory: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Не удалось создать рецепт: " + e.getMessage(), e);
        }
    }

    @Override
    public RecipeDto updateRecipe(Long id, String title, Long categoryId,
                                  List<String> ingredients, String description,
                                  List<Long> inventoryIds, boolean published) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("recipe.not_found", id)
                ));

        CategoryDto categoryDto = categoryService.getCategoryEntityForInternalUse(categoryId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("category.not_found", categoryId)
                ));

        Category category = createCategoryEntityFromDto(categoryDto);

        recipe.setTitle(title);
        recipe.setCategory(category);
        recipe.setDescription(description);
        recipe.setPublished(published);

        recipe.getIngredients().clear();
        if (ingredients != null) {
            recipe.getIngredients().addAll(ingredients);
        }

        recipe.getInventoryItems().clear();
        if (inventoryIds != null && !inventoryIds.isEmpty()) {
            List<InventoryDto> inventoryDtos = inventoryService.getInventoryByIdsForInternalUse(inventoryIds);
            for (InventoryDto inventoryDto : inventoryDtos) {
                Inventory inventory = createInventoryEntityFromDto(inventoryDto);
                recipe.addInventoryItem(inventory);
            }
        }

        Recipe savedRecipe = recipeRepository.save(recipe);
        return recipeConverter.toDto(savedRecipe);
    }

    private Category createCategoryEntityFromDto(CategoryDto categoryDto) {
        try {
            if (categoryDto == null) {
                throw new IllegalArgumentException("CategoryDto cannot be null");
            }

            Category category = new Category();
            category.setId(categoryDto.id());
            category.setName(categoryDto.name());
            category.setDescription(categoryDto.description());
            return category;
        } catch (Exception e) {
            System.err.println("Error creating Category entity from DTO: " + e.getMessage());
            throw new RuntimeException("Ошибка при создании категории", e);
        }
    }

    private Author createAuthorEntityFromDto(AuthorDto authorDto) {
        try {
            if (authorDto == null) {
                throw new IllegalArgumentException("AuthorDto cannot be null");
            }
            if (authorDto.user() == null) {
                throw new IllegalArgumentException("AuthorDto user cannot be null");
            }

            Author author = new Author();
            author.setId(authorDto.id());

            User user = new User();
            user.setId(authorDto.user().id());
            user.setUsername(authorDto.user().username());
            user.setEmail(authorDto.user().email());
            user.setEnabled(authorDto.user().enabled());

            if (authorDto.user().roles() != null) {
                user.setRoles(new HashSet<>(authorDto.user().roles()));
            }

            author.setUser(user);
            author.setBio(authorDto.bio());
            return author;
        } catch (Exception e) {
            System.err.println("Error creating Author entity from DTO: " + e.getMessage());
            throw new RuntimeException("Ошибка при создании автора", e);
        }
    }

    private Inventory createInventoryEntityFromDto(InventoryDto inventoryDto) {
        try {
            if (inventoryDto == null) {
                throw new IllegalArgumentException("InventoryDto cannot be null");
            }

            Inventory inventory = new Inventory();
            inventory.setId(inventoryDto.id());
            inventory.setName(inventoryDto.name());
            inventory.setDescription(inventoryDto.description());
            return inventory;
        } catch (Exception e) {
            System.err.println("Error creating Inventory entity from DTO: " + e.getMessage());
            throw new RuntimeException("Ошибка при создании инвентаря", e);
        }
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

    @Override
    public List<InventoryDto> getInventoryByRecipeId(Long recipeId) {
        return inventoryService.getInventoryByRecipeId(recipeId);
    }

    @Override
    public boolean isInventoryUsedInRecipes(Long inventoryId) {
        return inventoryService.isInventoryUsedInRecipes(inventoryId);
    }

    @Override
    public long getRecipeCountByInventoryId(Long inventoryId) {
        return inventoryService.getRecipeCountByInventory(inventoryId);
    }
}