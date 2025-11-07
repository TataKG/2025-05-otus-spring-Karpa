package ru.otus.hw.controllers.rest;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.otus.hw.dto.*;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.RecipeService;
import ru.otus.hw.util.MessageProvider;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService recipeService;
    private final MessageProvider messageProvider;
    private final CommentService commentService;
    private final AuthorService authorService;

    @GetMapping("/my-recipes")
    public ResponseEntity<ApiResponse<List<RecipeSummaryDto>>> getMyRecipes(Authentication authentication) {
        System.out.println("=== MY RECIPES ENDPOINT ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));

        if (authentication != null) {
            System.out.println("Username: " + authentication.getName());
            System.out.println("Authorities: " + authentication.getAuthorities());
        }

        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("User not authenticated"));
            }

            String username = authentication.getName();
            System.out.println("Loading recipes for authenticated user: " + username);

            AuthorDto author = authorService.getAuthorByUsername(username)
                    .orElseThrow(() -> {
                        System.out.println("Author not found for username: " + username);
                        return new EntityNotFoundException("Author not found for user: " + username);
                    });

            System.out.println("Found author: " + author.id() + " for user: " + username);

            List<RecipeSummaryDto> recipes = recipeService.getRecipesByAuthor(author.id());
            System.out.println("Found " + recipes.size() + " recipes for author: " + author.id());

            return ResponseEntity.ok(ApiResponse.success(recipes));
        } catch (Exception e) {
            System.err.println("Error loading my recipes: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to load your recipes: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RecipeDto>> createRecipe(@RequestBody CreateRecipeRequest request, Authentication authentication) {
        try {
            String username = authentication.getName();
            AuthorDto author = authorService.getAuthorByUsername(username)
                    .orElseThrow(() -> new EntityNotFoundException("Author not found for user: " + username));

            // Проверяем, что пользователь создает рецепт от своего имени
            if (!request.authorId().equals(author.id())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("You can only create recipes for yourself"));
            }

            RecipeDto recipeDto = recipeService.createRecipe(
                    request.title(),
                    request.categoryId(),
                    request.authorId(),
                    request.ingredients(),
                    request.description(),
                    request.published()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.success(recipeDto, messageProvider.getMessage("recipe.created"))
            );
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create recipe: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RecipeDto>> updateRecipe(
            @PathVariable Long id,
            @RequestBody UpdateRecipeRequest request,
            Authentication authentication) {

        try {
            String username = authentication.getName();

            RecipeDto existingRecipe = recipeService.getRecipeById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Recipe not found"));

            AuthorDto currentAuthor = authorService.getAuthorByUsername(username)
                    .orElseThrow(() -> new EntityNotFoundException("Author not found"));

            if (!existingRecipe.author().id().equals(currentAuthor.id())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("You can only update your own recipes"));
            }

            RecipeDto updatedRecipe = recipeService.updateRecipe(
                    id,
                    request.title(),
                    request.categoryId(),
                    request.ingredients(),
                    request.description(),
                    request.inventoryIds(),
                    request.published()
            );

            return ResponseEntity.ok(
                    ApiResponse.success(updatedRecipe, messageProvider.getMessage("recipe.updated"))
            );
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update recipe: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RecipeDto>> getRecipeById(@PathVariable Long id) {
        try {
            RecipeDto recipeDto = recipeService.getRecipeById(id)
                    .orElseThrow(() -> new EntityNotFoundException(
                            messageProvider.getMessage("recipe.not_found", id)
                    ));

            // Проверяем, опубликован ли рецепт или пользователь имеет права
            if (!recipeDto.published()) {
                // Здесь можно добавить проверку прав, если нужно
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Recipe not found or not published"));
            }

            return ResponseEntity.ok(ApiResponse.success(recipeDto));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}/detailed")
    public ResponseEntity<ApiResponse<RecipeDto>> getRecipeByIdWithAllRelations(@PathVariable Long id, Authentication authentication) {
        try {
            RecipeDto recipeDto = recipeService.getRecipeByIdWithAllRelations(id)
                    .orElseThrow(() -> new EntityNotFoundException(
                            messageProvider.getMessage("recipe.not_found", id)
                    ));

            // Для неопубликованных рецептов проверяем права доступа
            if (!recipeDto.published()) {
                if (authentication == null || !authentication.isAuthenticated()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("Recipe not found"));
                }

                String username = authentication.getName();
                AuthorDto currentAuthor = authorService.getAuthorByUsername(username)
                        .orElse(null);

                if (currentAuthor == null || !recipeDto.author().id().equals(currentAuthor.id())) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("Recipe not found"));
                }
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

    @GetMapping("/published")
    public ResponseEntity<ApiResponse<List<RecipeSummaryDto>>> getPublishedRecipes() {
        List<RecipeSummaryDto> recipes = recipeService.getPublishedRecipes();
        return ResponseEntity.ok(ApiResponse.success(recipes));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<RecipeSummaryDto>>> getRecipesByCategory(@PathVariable Long categoryId) {
        List<RecipeSummaryDto> recipes = recipeService.getPublishedRecipesByCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.success(recipes));
    }

    @GetMapping("/author/{authorId}")
    public ResponseEntity<ApiResponse<List<RecipeSummaryDto>>> getRecipesByAuthor(@PathVariable Long authorId, Authentication authentication) {
        try {
            List<RecipeSummaryDto> recipes;

            // Проверяем, запрашивает ли автор свои собственные рецепты
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                AuthorDto currentAuthor = authorService.getAuthorByUsername(username).orElse(null);

                if (currentAuthor != null && currentAuthor.id().equals(authorId)) {
                    // Автор запрашивает свои рецепты - показываем все
                    recipes = recipeService.getRecipesByAuthor(authorId);
                } else {
                    // Другой пользователь запрашивает рецепты автора - показываем только опубликованные
                    recipes = recipeService.getPublishedRecipesByAuthor(authorId);
                }
            } else {
                // Неавторизованный пользователь - только опубликованные
                recipes = recipeService.getPublishedRecipesByAuthor(authorId);
            }

            return ResponseEntity.ok(ApiResponse.success(recipes));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to load recipes: " + e.getMessage()));
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

    @GetMapping("/search/filter")
    public ResponseEntity<ApiResponse<List<RecipeSummaryDto>>> searchRecipesByFilters(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long authorId,
            Authentication authentication) {

        List<RecipeSummaryDto> recipes;

        // Если указан автор и пользователь запрашивает свои рецепты, показываем все
        if (authorId != null && authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            AuthorDto currentAuthor = authorService.getAuthorByUsername(username).orElse(null);

            if (currentAuthor != null && currentAuthor.id().equals(authorId)) {
                recipes = recipeService.findRecipesByFilters(title, categoryId, authorId);
            } else {
                recipes = recipeService.findPublishedRecipesByFilters(title, categoryId, authorId);
            }
        } else {
            recipes = recipeService.findPublishedRecipesByFilters(title, categoryId, authorId);
        }

        return ResponseEntity.ok(ApiResponse.success(recipes));
    }

    @PutMapping("/{recipeId}/inventory")
    public ResponseEntity<ApiResponse<RecipeDto>> addInventoryToRecipe(
            @PathVariable Long recipeId,
            @RequestBody List<Long> inventoryIds,
            Authentication authentication) {

        try {
            // Проверяем права доступа
            RecipeDto existingRecipe = recipeService.getRecipeById(recipeId)
                    .orElseThrow(() -> new EntityNotFoundException("Recipe not found"));

            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                AuthorDto currentAuthor = authorService.getAuthorByUsername(username)
                        .orElseThrow(() -> new EntityNotFoundException("Author not found"));

                if (!existingRecipe.author().id().equals(currentAuthor.id())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(ApiResponse.error("You can only update your own recipes"));
                }
            }

            RecipeDto recipeDto = recipeService.addInventoryToRecipe(recipeId, inventoryIds);
            return ResponseEntity.ok(
                    ApiResponse.success(recipeDto, messageProvider.getMessage("recipe.inventory_added"))
            );
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to add inventory: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRecipe(@PathVariable Long id, Authentication authentication) {
        try {
            System.out.println("=== DELETE RECIPE ENDPOINT CALLED ===");
            System.out.println("Deleting recipe ID: " + id);

            // Проверяем права доступа
            RecipeDto existingRecipe = recipeService.getRecipeById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Recipe not found"));

            String username = authentication.getName();
            AuthorDto currentAuthor = authorService.getAuthorByUsername(username)
                    .orElseThrow(() -> new EntityNotFoundException("Author not found"));

            if (!existingRecipe.author().id().equals(currentAuthor.id())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("You can only delete your own recipes"));
            }

            recipeService.deleteRecipe(id);

            System.out.println("Recipe deletion completed successfully");

            // ВСЕГДА возвращаем 200 OK даже если рецепт уже удален
            return ResponseEntity.ok(
                    ApiResponse.success(null, "Recipe successfully deleted")
            );

        } catch (EntityNotFoundException e) {
            System.err.println("Recipe not found: " + e.getMessage());
            // Вместо 404 возвращаем успех, так как цель (удаление) достигнута
            return ResponseEntity.ok(
                    ApiResponse.success(null, "Recipe was already deleted or not found")
            );
        } catch (Exception e) {
            System.err.println("Unexpected error deleting recipe: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete recipe: " + e.getMessage()));
        }
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<RecipeDto>> publishRecipe(@PathVariable Long id, Authentication authentication) {
        try {
            RecipeDto existingRecipe = recipeService.getRecipeById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Recipe not found"));

            String username = authentication.getName();
            AuthorDto currentAuthor = authorService.getAuthorByUsername(username)
                    .orElseThrow(() -> new EntityNotFoundException("Author not found"));

            if (!existingRecipe.author().id().equals(currentAuthor.id())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("You can only publish your own recipes"));
            }

            RecipeDto updatedRecipe = recipeService.updateRecipe(
                    id,
                    existingRecipe.title(),
                    existingRecipe.category().id(),
                    existingRecipe.ingredients(),
                    existingRecipe.description(),
                    existingRecipe.inventoryItems().stream()
                            .map(InventoryDto::id)
                            .collect(Collectors.toList()),
                    true // published = true
            );

            return ResponseEntity.ok(
                    ApiResponse.success(updatedRecipe, "Recipe published successfully")
            );
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to publish recipe: " + e.getMessage()));
        }
    }

    @PatchMapping("/{id}/unpublish")
    public ResponseEntity<ApiResponse<RecipeDto>> unpublishRecipe(@PathVariable Long id, Authentication authentication) {
        try {
            RecipeDto existingRecipe = recipeService.getRecipeById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Recipe not found"));

            String username = authentication.getName();
            AuthorDto currentAuthor = authorService.getAuthorByUsername(username)
                    .orElseThrow(() -> new EntityNotFoundException("Author not found"));

            if (!existingRecipe.author().id().equals(currentAuthor.id())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("You can only unpublish your own recipes"));
            }

            RecipeDto updatedRecipe = recipeService.updateRecipe(
                    id,
                    existingRecipe.title(),
                    existingRecipe.category().id(),
                    existingRecipe.ingredients(),
                    existingRecipe.description(),
                    existingRecipe.inventoryItems().stream()
                            .map(InventoryDto::id)
                            .collect(Collectors.toList()),
                    false // published = false
            );

            return ResponseEntity.ok(
                    ApiResponse.success(updatedRecipe, "Recipe unpublished successfully")
            );
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to unpublish recipe: " + e.getMessage()));
        }
    }

    public record CreateRecipeRequest(
            String title,
            Long categoryId,
            Long authorId,
            List<String> ingredients,
            String description,
            boolean published
    ) {}

    public record UpdateRecipeRequest(
            String title,
            Long categoryId,
            List<String> ingredients,
            String description,
            List<Long> inventoryIds,
            boolean published
    ) {}
}