package ru.otus.hw.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.otus.hw.dto.CategoryDto;
import ru.otus.hw.dto.CategoryWithUsageDto;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    CategoryDto createCategory(String name);

    Optional<CategoryDto> getCategoryById(Long id);

    Optional<CategoryDto> getCategoryByName(String name);

    List<CategoryDto> getAllCategories();

    Page<CategoryDto> getAllCategories(Pageable pageable);

    List<CategoryDto> findCategoriesByNameContaining(String name);

    boolean categoryExists(String name);

    CategoryDto createCategoryWithDescription(String name, String description);

    CategoryDto updateCategory(Long id, String name, String description);

    void deleteCategory(Long id);

    boolean isCategoryUsedInRecipes(Long categoryId);

    long getRecipeCountByCategory(Long categoryId);

    long getPublishedRecipeCountByCategory(Long categoryId);

    List<CategoryDto> getUnusedCategories();

    List<CategoryWithUsageDto> getCategoriesWithUsage();

    Optional<CategoryWithUsageDto> getCategoryWithUsageById(Long id);
}
