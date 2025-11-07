package ru.otus.hw.controllers.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.otus.hw.dto.ApiResponse;
import ru.otus.hw.dto.CategoryDto;
import ru.otus.hw.services.CategoryService;
import ru.otus.hw.util.MessageProvider;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;
    private final MessageProvider messageProvider;

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryDto>> updateCategory(
            @PathVariable Long id,
            @RequestBody UpdateCategoryRequest request) {

        CategoryDto categoryDto = categoryService.updateCategory(id, request.name(), request.description());
        return ResponseEntity.ok(
                ApiResponse.success(categoryDto, messageProvider.getMessage("category.updated"))
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(
                ApiResponse.success(null, messageProvider.getMessage("category.deleted"))
        );
    }

    @GetMapping("/{id}/usage")
    public ResponseEntity<ApiResponse<CategoryUsageResponse>> getCategoryUsage(@PathVariable Long id) {
        boolean isUsed = categoryService.isCategoryUsedInRecipes(id);
        long recipeCount = categoryService.getRecipeCountByCategory(id);

        CategoryUsageResponse usage = new CategoryUsageResponse(isUsed, recipeCount);
        return ResponseEntity.ok(ApiResponse.success(usage));
    }

    public record UpdateCategoryRequest(String name, String description) {}
    public record CategoryUsageResponse(boolean isUsed, long recipeCount) {}
}
