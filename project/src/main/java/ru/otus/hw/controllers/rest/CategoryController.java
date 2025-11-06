package ru.otus.hw.controllers.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.otus.hw.dto.ApiResponse;
import ru.otus.hw.dto.CategoryDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.CategoryService;
import ru.otus.hw.util.MessageProvider;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final MessageProvider messageProvider;

    public CategoryController(CategoryService categoryService, MessageProvider messageProvider) {
        this.categoryService = categoryService;
        this.messageProvider = messageProvider;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryDto>> createCategory(@RequestBody CreateCategoryRequest request) {
        CategoryDto categoryDto = categoryService.createCategory(request.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(categoryDto, messageProvider.getMessage("category.created"))
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryDto>> getCategoryById(@PathVariable Long id) {
        CategoryDto categoryDto = categoryService.getCategoryById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("category.not_found", id)
                ));
        return ResponseEntity.ok(ApiResponse.success(categoryDto));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<ApiResponse<CategoryDto>> getCategoryByName(@PathVariable String name) {
        CategoryDto categoryDto = categoryService.getCategoryByName(name)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("category.not_found.name", name)
                ));
        return ResponseEntity.ok(ApiResponse.success(categoryDto));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getAllCategories() {
        List<CategoryDto> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/exists/{name}")
    public ResponseEntity<ApiResponse<Boolean>> checkCategoryExists(@PathVariable String name) {
        boolean exists = categoryService.categoryExists(name);
        return ResponseEntity.ok(ApiResponse.success(exists));
    }

    public record CreateCategoryRequest(String name) {}
}
