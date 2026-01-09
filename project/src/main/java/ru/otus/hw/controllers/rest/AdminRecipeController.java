package ru.otus.hw.controllers.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.hw.dto.ApiResponse;
import ru.otus.hw.dto.RecipeDto;
import ru.otus.hw.services.RecipeService;
import ru.otus.hw.utils.MessageProvider;

import java.util.List;

@RestController
@RequestMapping("/api/admin/recipes")
@RequiredArgsConstructor
public class AdminRecipeController {

    private final RecipeService recipeService;
    private final MessageProvider messageProvider;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RecipeDto>>> getPublishedRecipes(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long authorId) {

        try {
            List<RecipeDto> recipes = recipeService.findPublishedRecipesWithFilters(search, categoryId, authorId);
            return ResponseEntity.ok(ApiResponse.success(recipes));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(messageProvider.getMessage("recipes.load_error") + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRecipe(@PathVariable Long id) {
        try {
            recipeService.deleteRecipe(id);
            return ResponseEntity.ok(ApiResponse.success(null, messageProvider.getMessage("recipe.deleted")));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(messageProvider.getMessage("recipe.delete_error") + e.getMessage()));
        }
    }
}