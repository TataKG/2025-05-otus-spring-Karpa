package ru.otus.hw.services;

import ru.otus.hw.dto.CategoryDto;
import ru.otus.hw.dto.CategoryWithUsageDto;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    CategoryDto createCategory(String name, String description);

    Optional<CategoryDto> getCategoryById(Long id);

    Optional<CategoryDto> getCategoryEntityForInternalUse(Long id); // переименовать!

    Optional<CategoryDto> getCategoryByName(String name);

    List<CategoryDto> getAllCategories();

    CategoryDto updateCategory(Long id, String name, String description);

    void deleteCategory(Long id);

    boolean isCategoryUsedInRecipes(Long categoryId);

    long getRecipeCountByCategory(Long categoryId);

    List<CategoryDto> getUnusedCategories();

    List<CategoryWithUsageDto> getCategoriesWithUsage();
}