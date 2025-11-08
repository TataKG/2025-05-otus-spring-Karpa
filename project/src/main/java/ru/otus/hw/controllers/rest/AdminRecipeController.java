package ru.otus.hw.controllers.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.otus.hw.dto.ApiResponse;
import ru.otus.hw.dto.RecipeDto;
import ru.otus.hw.services.RecipeService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/recipes")
@RequiredArgsConstructor
public class AdminRecipeController {

    private final RecipeService recipeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RecipeDto>>> getPublishedRecipes(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long authorId) {

        try {
            List<RecipeDto> recipes = recipeService.findPublishedRecipesWithFilters(search, categoryId, authorId);
            return ResponseEntity.ok(ApiResponse.success(recipes));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Ошибка загрузки рецептов: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRecipe(@PathVariable Long id) {
        try {
            recipeService.deleteRecipe(id);
            return ResponseEntity.ok(ApiResponse.success(null, "Рецепт успешно удален"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Ошибка при удалении рецепта: " + e.getMessage()));
        }
    }
}