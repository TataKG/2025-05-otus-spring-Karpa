package ru.otus.hw.controllers.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.otus.hw.dto.ApiResponse;
import ru.otus.hw.dto.CategoryDto;
import ru.otus.hw.exceptions.EntityAlreadyExistsException;
import ru.otus.hw.services.CategoryService;
import ru.otus.hw.util.MessageProvider;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;
    private final MessageProvider messageProvider;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getAllCategories() {
        List<CategoryDto> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryDto>> createCategory(@RequestBody CreateCategoryRequest request) {
        try {
            System.out.println("Creating category: " + request.name() + ", " + request.description());

            if (request.name() == null || request.name().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Название категории не может быть пустым"));
            }

            CategoryDto categoryDto = categoryService.createCategoryWithDescription(
                    request.name().trim(),
                    request.description() != null ? request.description().trim() : null
            );

            System.out.println("Category created successfully: " + categoryDto.id());
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.success(categoryDto, messageProvider.getMessage("category.created"))
            );
        } catch (EntityAlreadyExistsException e) {
            System.out.println("Category already exists: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            System.out.println("Error creating category: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Ошибка при создании категории: " + e.getMessage()));
        }
    }

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
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok(
                    ApiResponse.success(null, messageProvider.getMessage("category.deleted"))
            );
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}/usage")
    public ResponseEntity<ApiResponse<CategoryUsageResponse>> getCategoryUsage(@PathVariable Long id) {
        boolean isUsed = categoryService.isCategoryUsedInRecipes(id);
        long recipeCount = categoryService.getRecipeCountByCategory(id);

        CategoryUsageResponse usage = new CategoryUsageResponse(isUsed, recipeCount);
        return ResponseEntity.ok(ApiResponse.success(usage));
    }

    public record CreateCategoryRequest(String name, String description) {}
    public record UpdateCategoryRequest(String name, String description) {}
    public record CategoryUsageResponse(boolean isUsed, long recipeCount) {}
}
