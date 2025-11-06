package ru.otus.hw.controllers.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.otus.hw.dto.ApiResponse;
import ru.otus.hw.dto.RecipeDto;
import ru.otus.hw.dto.RecipeSummaryDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.RecipeService;
import ru.otus.hw.util.MessageProvider;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService recipeService;
    private final MessageProvider messageProvider;

    public RecipeController(RecipeService recipeService, MessageProvider messageProvider) {
        this.recipeService = recipeService;
        this.messageProvider = messageProvider;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RecipeDto>> createRecipe(@RequestBody CreateRecipeRequest request) {
        RecipeDto recipeDto = recipeService.createRecipe(
                request.title(),
                request.categoryId(),
                request.authorId(),
                request.ingredients(),
                request.description()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(recipeDto, messageProvider.getMessage("recipe.created"))
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RecipeDto>> getRecipeById(@PathVariable Long id) {
        RecipeDto recipeDto = recipeService.getRecipeById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("recipe.not_found", id)
                ));
        return ResponseEntity.ok(ApiResponse.success(recipeDto));
    }

    @GetMapping("/{id}/detailed")
    public ResponseEntity<ApiResponse<RecipeDto>> getRecipeByIdWithAllRelations(@PathVariable Long id) {
        RecipeDto recipeDto = recipeService.getRecipeByIdWithAllRelations(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("recipe.not_found", id)
                ));
        return ResponseEntity.ok(ApiResponse.success(recipeDto));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RecipeSummaryDto>>> getAllRecipes() {
        System.out.println("=== Запрос всех рецептов ===");
        List<RecipeSummaryDto> recipes = recipeService.getAllRecipes();
        System.out.println("Найдено рецептов: " + recipes.size());
        recipes.forEach(recipe -> System.out.println("Рецепт: " + recipe.title()));
        return ResponseEntity.ok(ApiResponse.success(recipes));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<RecipeSummaryDto>>> getRecipesByCategory(@PathVariable Long categoryId) {
        List<RecipeSummaryDto> recipes = recipeService.getRecipesByCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.success(recipes));
    }

    @GetMapping("/author/{authorId}")
    public ResponseEntity<ApiResponse<List<RecipeSummaryDto>>> getRecipesByAuthor(@PathVariable Long authorId) {
        List<RecipeSummaryDto> recipes = recipeService.getRecipesByAuthor(authorId);
        return ResponseEntity.ok(ApiResponse.success(recipes));
    }

    @GetMapping("/search/title")
    public ResponseEntity<ApiResponse<List<RecipeSummaryDto>>> searchRecipesByTitle(
            @RequestParam String title) {
        List<RecipeSummaryDto> recipes = recipeService.searchRecipesByTitle(title);
        return ResponseEntity.ok(ApiResponse.success(recipes));
    }

    @GetMapping("/search/ingredient")
    public ResponseEntity<ApiResponse<List<RecipeSummaryDto>>> searchRecipesByIngredient(
            @RequestParam String ingredient) {
        List<RecipeSummaryDto> recipes = recipeService.searchRecipesByIngredient(ingredient);
        return ResponseEntity.ok(ApiResponse.success(recipes));
    }

    @GetMapping("/search/filter")
    public ResponseEntity<ApiResponse<List<RecipeSummaryDto>>> searchRecipesByFilters(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long authorId) {
        List<RecipeSummaryDto> recipes = recipeService.findRecipesByFilters(title, categoryId, authorId);
        return ResponseEntity.ok(ApiResponse.success(recipes));
    }

    @PutMapping("/{recipeId}/inventory")
    public ResponseEntity<ApiResponse<RecipeDto>> addInventoryToRecipe(
            @PathVariable Long recipeId,
            @RequestBody List<Long> inventoryIds) {
        RecipeDto recipeDto = recipeService.addInventoryToRecipe(recipeId, inventoryIds);
        return ResponseEntity.ok(
                ApiResponse.success(recipeDto, messageProvider.getMessage("recipe.inventory_added"))
        );
    }

    public record CreateRecipeRequest(
            String title,
            Long categoryId,
            Long authorId,
            List<String> ingredients,
            String description
    ) {}
}
