package ru.otus.hw.services;

import ru.otus.hw.dto.CategoryDto;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    CategoryDto createCategory(String name);

    Optional<CategoryDto> getCategoryById(Long id);

    Optional<CategoryDto> getCategoryByName(String name);

    List<CategoryDto> getAllCategories();

    boolean categoryExists(String name);
}
