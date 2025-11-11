package ru.otus.hw.controllers.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.hw.dto.*;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.CategoryService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.InventoryService;
import ru.otus.hw.services.RecipeService;
import ru.otus.hw.util.MessageProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService recipeService;
    private final MessageProvider messageProvider;
    private final CommentService commentService;
    private final AuthorService authorService;
    private final CategoryService categoryService;
    private final InventoryService inventoryService;

    @GetMapping("/create-form-data")
    public ResponseEntity<ApiResponse<RecipeFormData>> getCreateFormData(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error(messageProvider.getMessage("user.not_authenticated")));
            }

            String username = authentication.getName();
            System.out.println("Loading create form data for user: " + username); // Логирование

            Optional<AuthorDto> authorOpt = authorService.getAuthorByUsername(username);

            if (authorOpt.isEmpty()) {
                System.err.println("Author not found for username: " + username); // Логирование
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Автор не найден для пользователя: " + username));
            }

            AuthorDto author = authorOpt.get();
            System.out.println("Found author: " + author.id() + " for user: " + username); // Логирование

            RecipeDto emptyRecipe = createEmptyRecipeDto(author);

            List<CategoryDto> categories = categoryService.getAllCategories();
            List<InventoryDto> inventory = inventoryService.getAllInventory();

            RecipeFormData formData = new RecipeFormData(emptyRecipe, categories, inventory);

            return ResponseEntity.ok(ApiResponse.success(formData));
        } catch (Exception e) {
            System.err.println("Error in getCreateFormData: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Ошибка при загрузке данных формы: " + e.getMessage()));
        }
    }

    @GetMapping("/edit-form-data/{id}")
    public ResponseEntity<ApiResponse<RecipeFormData>> getEditFormData(@PathVariable Long id, Authentication authentication) {
        try {
            String username = authentication.getName();
            RecipeDto recipe = getRecipeAndCheckOwnership(id, username);

            RecipeFormData formData = new RecipeFormData(
                    recipe,
                    categoryService.getAllCategories(),
                    inventoryService.getAllInventory()
            );

            return ResponseEntity.ok(ApiResponse.success(formData));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(messageProvider.getMessage("recipe.edit.form_data.load_failed")));
        }
    }

    @GetMapping("/my-recipes")
    public ResponseEntity<ApiResponse<List<RecipeSummaryDto>>> getMyRecipes(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error(messageProvider.getMessage("user.not_authenticated")));
            }

            String username = authentication.getName();
            List<RecipeSummaryDto> recipes = getRecipesForCurrentUser(username);

            return ResponseEntity.ok(ApiResponse.success(recipes));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(messageProvider.getMessage("recipe.my_recipes.load_failed")));
        }
    }

    @PostMapping(value = {"", "/"})
    public ResponseEntity<ApiResponse<RecipeDto>> createRecipe(@RequestBody CreateRecipeRequest request, Authentication authentication) {
        System.out.println("=== DEBUG BREAKPOINT ===");
        new RuntimeException("DEBUG BREAKPOINT").printStackTrace();

        try {
            System.out.println("=== CREATE RECIPE REQUEST ===");
            System.out.println("Request data: " + request);
            System.out.println("User: " + authentication.getName());

            String username = authentication.getName();
            AuthorDto author = authorService.getAuthorByUsername(username)
                    .orElseThrow(() -> {
                        System.err.println("Author not found for user: " + username);
                        return new EntityNotFoundException(messageProvider.getMessage("author.not_found"));
                    });

            System.out.println("Author found: ID=" + author.id() + ", Request authorId: " + request.authorId());

            validateAuthorOwnership(request.authorId(), author.id());

            // Детальная валидация с лучшими сообщениями об ошибках
            if (request.title() == null || request.title().trim().isEmpty()) {
                throw new IllegalArgumentException("Название рецепта обязательно");
            }
            if (request.title().trim().length() < 2) {
                throw new IllegalArgumentException("Название рецепта должно содержать минимум 2 символа");
            }
            if (request.categoryId() == null) {
                throw new IllegalArgumentException("Категория обязательна");
            }
            if (request.description() == null || request.description().trim().isEmpty()) {
                throw new IllegalArgumentException("Описание рецепта обязательно");
            }
            if (request.description().trim().length() < 10) {
                throw new IllegalArgumentException("Описание рецепта должно содержать минимум 10 символов");
            }
            if (request.ingredients() == null || request.ingredients().isEmpty()) {
                throw new IllegalArgumentException("Добавьте хотя бы один ингредиент");
            }

            // Проверяем, что есть хотя бы один непустой ингредиент
            boolean hasValidIngredients = request.ingredients().stream()
                    .anyMatch(ingredient -> ingredient != null && !ingredient.trim().isEmpty());
            if (!hasValidIngredients) {
                throw new IllegalArgumentException("Добавьте хотя бы один непустой ингредиент");
            }

            System.out.println("Calling recipeService.createRecipeWithInventory...");
            System.out.println("Ingredients: " + request.ingredients());
            System.out.println("Inventory IDs: " + request.inventoryIds());
            System.out.println("Inventory IDs size: " + (request.inventoryIds() != null ? request.inventoryIds().size() : 0));

            RecipeDto recipeDto = recipeService.createRecipeWithInventory(
                    request.title().trim(),
                    request.categoryId(),
                    request.authorId(),
                    request.ingredients(),
                    request.description().trim(),
                    request.inventoryIds(),
                    request.published()
            );

            System.out.println("Recipe created successfully: " + recipeDto.id());

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.success(recipeDto, messageProvider.getMessage("recipe.created"))
            );
        } catch (EntityNotFoundException e) {
            System.err.println("EntityNotFoundException: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (SecurityException e) {
            System.err.println("SecurityException: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            System.err.println("IllegalArgumentException: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            System.err.println("Unexpected error in createRecipe: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Внутренняя ошибка сервера при создании рецепта: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RecipeDto>> updateRecipe(
            @PathVariable Long id,
            @RequestBody UpdateRecipeRequest request,
            Authentication authentication) {

        try {
            String username = authentication.getName();
            getRecipeAndCheckOwnership(id, username);

            RecipeDto updatedRecipe = recipeService.updateRecipe(
                    id,
                    request.title(),
                    request.categoryId(),
                    request.ingredients(),
                    request.description(),
                    request.inventoryIds(),
                    request.published()
            );

            return ResponseEntity.ok(ApiResponse.success(updatedRecipe, messageProvider.getMessage("recipe.updated")));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(messageProvider.getMessage("recipe.update.failed")));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RecipeDto>> getRecipeById(@PathVariable Long id) {
        try {
            RecipeDto recipeDto = recipeService.getRecipeById(id)
                    .orElseThrow(() -> new EntityNotFoundException(
                            messageProvider.getMessage("recipe.not_found", id)
                    ));

            if (!recipeDto.published()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(messageProvider.getMessage("recipe.not_found_or_unpublished")));
            }

            return ResponseEntity.ok(ApiResponse.success(recipeDto));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RecipeSummaryDto>>> getAllRecipes() {
        List<RecipeSummaryDto> recipes = recipeService.getAllPublishedRecipes();
        return ResponseEntity.ok(ApiResponse.success(recipes));
    }

    @GetMapping("/author/{authorId}")
    public ResponseEntity<ApiResponse<List<RecipeSummaryDto>>> getRecipesByAuthor(@PathVariable Long authorId, Authentication authentication) {
        try {
            List<RecipeSummaryDto> recipes = getRecipesForAuthor(authorId, authentication);
            return ResponseEntity.ok(ApiResponse.success(recipes));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(messageProvider.getMessage("recipe.load_failed")));
        }
    }

    @GetMapping("/{id}/comment-count")
    public ResponseEntity<ApiResponse<Integer>> getCommentCount(@PathVariable Long id) {
        int count = commentService.getCommentCountForRecipe(id);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/search/title")
    public ResponseEntity<ApiResponse<List<RecipeSummaryDto>>> searchRecipesByTitle(
            @RequestParam String title) {
        List<RecipeSummaryDto> recipes = recipeService.searchPublishedRecipesByTitle(title);
        return ResponseEntity.ok(ApiResponse.success(recipes));
    }

    @GetMapping("/search/ingredient")
    public ResponseEntity<ApiResponse<List<RecipeSummaryDto>>> searchRecipesByIngredient(
            @RequestParam String ingredient) {
        List<RecipeSummaryDto> recipes = recipeService.searchPublishedRecipesByIngredient(ingredient);
        return ResponseEntity.ok(ApiResponse.success(recipes));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRecipe(@PathVariable Long id, Authentication authentication) {
        try {
            String username = authentication.getName();
            getRecipeAndCheckOwnership(id, username);

            recipeService.deleteRecipe(id);

            return ResponseEntity.ok(
                    ApiResponse.success(null, messageProvider.getMessage("recipe.deleted"))
            );
        } catch (EntityNotFoundException e) {
            return ResponseEntity.ok(
                    ApiResponse.success(null, messageProvider.getMessage("recipe.already_deleted"))
            );
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(messageProvider.getMessage("recipe.delete.failed")));
        }
    }

    @GetMapping("/{id}/detailed")
    public ResponseEntity<ApiResponse<RecipeWithDetailsDto>> getRecipeWithDetails(@PathVariable Long id) {
        try {
            RecipeWithDetailsDto recipeWithDetails = recipeService.getRecipeWithDetails(id);
            return ResponseEntity.ok(ApiResponse.success(recipeWithDetails));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(messageProvider.getMessage("recipe.details.load_failed")));
        }
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<RecipeDto>> publishRecipe(@PathVariable Long id, Authentication authentication) {
        try {
            String username = authentication.getName();
            RecipeDto existingRecipe = getRecipeAndCheckOwnership(id, username);

            RecipeDto updatedRecipe = recipeService.publishRecipe(id);

            return ResponseEntity.ok(
                    ApiResponse.success(updatedRecipe, messageProvider.getMessage("recipe.published"))
            );
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(messageProvider.getMessage("recipe.publish.failed")));
        }
    }

    @PatchMapping("/{id}/unpublish")
    public ResponseEntity<ApiResponse<RecipeDto>> unpublishRecipe(@PathVariable Long id, Authentication authentication) {
        try {
            String username = authentication.getName();
            RecipeDto existingRecipe = getRecipeAndCheckOwnership(id, username);

            RecipeDto updatedRecipe = recipeService.unpublishRecipe(id);

            return ResponseEntity.ok(
                    ApiResponse.success(updatedRecipe, messageProvider.getMessage("recipe.unpublished"))
            );
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(messageProvider.getMessage("recipe.unpublish.failed")));
        }
    }

    private RecipeDto createEmptyRecipeDto(AuthorDto author) {
        return new RecipeDto(
                null,
                "",
                null,
                author,
                new ArrayList<>(),
                new ArrayList<>(),
                "",
                0,
                false,
                null,
                null
        );
    }

    private RecipeDto getRecipeAndCheckOwnership(Long recipeId, String username) {
        RecipeDto recipe = recipeService.getRecipeById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException(messageProvider.getMessage("recipe.not_found")));

        AuthorDto currentAuthor = authorService.getAuthorByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(messageProvider.getMessage("author.not_found")));

        if (!recipe.author().id().equals(currentAuthor.id())) {
            throw new SecurityException(messageProvider.getMessage("recipe.operation.own_only"));
        }

        return recipe;
    }

    private void validateAuthorOwnership(Long requestAuthorId, Long currentAuthorId) {
        if (!requestAuthorId.equals(currentAuthorId)) {
            throw new SecurityException(messageProvider.getMessage("recipe.create.own_only"));
        }
    }

    private List<RecipeSummaryDto> getRecipesForCurrentUser(String username) {
        Optional<AuthorDto> authorOpt = authorService.getAuthorByUsername(username);
        if (authorOpt.isEmpty()) {
            return List.of();
        }

        AuthorDto author = authorOpt.get();
        return recipeService.getRecipesByAuthor(author.id());
    }

    private List<RecipeSummaryDto> getRecipesForAuthor(Long authorId, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            AuthorDto currentAuthor = authorService.getAuthorByUsername(username).orElse(null);

            if (currentAuthor != null && currentAuthor.id().equals(authorId)) {
                return recipeService.getRecipesByAuthor(authorId);
            }
        }
        return recipeService.getPublishedRecipesByAuthor(authorId);
    }

    public record CreateRecipeRequest(
            String title,
            Long categoryId,
            Long authorId,
            List<String> ingredients,
            List<Long> inventoryIds, // Убедитесь, что это List<Long>
            String description,
            boolean published
    ) {
        // Добавьте конструктор с проверками
        public CreateRecipeRequest {
            // Валидация обязательных полей
            if (title == null || title.trim().isEmpty()) {
                throw new IllegalArgumentException("Title cannot be null or empty");
            }
            if (categoryId == null) {
                throw new IllegalArgumentException("CategoryId cannot be null");
            }
            if (authorId == null) {
                throw new IllegalArgumentException("AuthorId cannot be null");
            }
            if (ingredients == null || ingredients.isEmpty()) {
                throw new IllegalArgumentException("Ingredients cannot be null or empty");
            }
            if (description == null || description.trim().isEmpty()) {
                throw new IllegalArgumentException("Description cannot be null or empty");
            }

            // Нормализация inventoryIds
            inventoryIds = (inventoryIds != null) ? inventoryIds : new ArrayList<>();
        }
    }

    public record UpdateRecipeRequest(
            String title,
            Long categoryId,
            List<String> ingredients,
            String description,
            List<Long> inventoryIds,
            boolean published
    ) {
    }

    public record RecipeFormData(
            RecipeDto recipe,
            List<CategoryDto> categories,
            List<InventoryDto> inventoryItems
    ) {
    }
}