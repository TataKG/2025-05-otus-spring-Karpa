package ru.otus.hw.controllers.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.hw.dto.ApiResponse;
import ru.otus.hw.dto.CategoryDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.CategoryService;
import ru.otus.hw.utils.MessageProvider;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final MessageProvider messageProvider;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryDto>> getCategoryById(@PathVariable Long id) {
        try {
            CategoryDto categoryDto = categoryService.getCategoryById(id)
                    .orElseThrow(() -> new EntityNotFoundException(
                            messageProvider.getMessage("category.not_found", id)
                    ));
            return ResponseEntity.ok(ApiResponse.success(categoryDto));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<ApiResponse<CategoryDto>> getCategoryByName(@PathVariable String name) {
        try {
            CategoryDto categoryDto = categoryService.getCategoryByName(name)
                    .orElseThrow(() -> new EntityNotFoundException(
                            messageProvider.getMessage("category.not_found.name", name)
                    ));
            return ResponseEntity.ok(ApiResponse.success(categoryDto));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getAllCategories() {
        try {
            List<CategoryDto> categories = categoryService.getAllCategories();
            return ResponseEntity.ok(ApiResponse.success(categories));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(messageProvider.getMessage("categories.load_error")));
        }
    }

    @GetMapping("/exists/{name}")
    public ResponseEntity<ApiResponse<Boolean>> checkCategoryExists(@PathVariable String name) {
        boolean exists = categoryService.getCategoryByName(name).isPresent();
        return ResponseEntity.ok(ApiResponse.success(exists));
    }
}