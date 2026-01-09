package ru.otus.hw.controllers.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.hw.dto.ApiResponse;
import ru.otus.hw.dto.CategoryDto;
import ru.otus.hw.exceptions.EntityAlreadyExistsException;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.CategoryService;
import ru.otus.hw.utils.MessageProvider;

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
    public ResponseEntity<ApiResponse<CategoryDto>> createCategory(
            @Valid @RequestBody CategoryRequest request) {
        try {
            CategoryDto categoryDto = categoryService.createCategory(request.name(), request.description());
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.success(categoryDto, messageProvider.getMessage("category.created"))
            );
        } catch (EntityAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryDto>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        try {
            CategoryDto categoryDto = categoryService.updateCategory(id, request.name(), request.description());
            return ResponseEntity.ok(
                    ApiResponse.success(categoryDto, messageProvider.getMessage("category.updated"))
            );
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (EntityAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok(
                    ApiResponse.success(null, messageProvider.getMessage("category.deleted"))
            );
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}/usage")
    public ResponseEntity<ApiResponse<CategoryUsageResponse>> getCategoryUsage(@PathVariable Long id) {
        try {
            boolean isUsed = categoryService.isCategoryUsedInRecipes(id);
            long recipeCount = categoryService.getRecipeCountByCategory(id);
            CategoryUsageResponse usage = new CategoryUsageResponse(isUsed, recipeCount);
            return ResponseEntity.ok(ApiResponse.success(usage));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(messageProvider.getMessage("category.not_found", id)));
        }
    }

    public record CategoryRequest(
            @NotBlank(message = "{category.name.not.blank}")
            @Size(min = 2, max = 50, message = "{category.name.size}")
            @Pattern(regexp = "^[a-zA-Zа-яА-Я0-9\\s\\-]+$", message = "{category.name.pattern}")
            String name,

            @NotBlank(message = "{category.description.not.blank}")
            @Size(max = 255, message = "{category.description.size}")
            String description
    ) {
        public CategoryRequest {
            if (name != null) {
                name = name.trim();
            }
            if (description != null) {
                description = description.trim();
            }
        }
    }

    public record CategoryUsageResponse(boolean isUsed, long recipeCount) {
    }
}