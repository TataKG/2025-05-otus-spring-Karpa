package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.converters.CategoryConverter;
import ru.otus.hw.dto.CategoryDto;
import ru.otus.hw.dto.CategoryWithUsageDto;
import ru.otus.hw.exceptions.EntityAlreadyExistsException;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Category;
import ru.otus.hw.repositories.CategoryRepository;
import ru.otus.hw.util.MessageProvider;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryConverter categoryConverter;
    private final MessageProvider messageProvider;

    @Override
    public CategoryDto createCategory(String name, String description) {
        validateCategoryName(name);
        String trimmedName = name.trim();

        checkCategoryExists(trimmedName);

        Category category = new Category(trimmedName, description != null ? description.trim() : null);
        Category savedCategory = categoryRepository.save(category);
        return categoryConverter.toDto(savedCategory);
    }

    @Override
    public Optional<CategoryDto> getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(categoryConverter::toDto);
    }

    @Override
    public Optional<CategoryDto> getCategoryEntityForInternalUse(Long id) {
        return categoryRepository.findById(id)
                .map(categoryConverter::toDto);
    }

    @Override
    public Optional<CategoryDto> getCategoryByName(String name) {
        return categoryRepository.findByName(name)
                .map(categoryConverter::toDto);
    }

    @Override
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryConverter::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto updateCategory(Long id, String name, String description) {
        Category category = getCategoryEntity(id);
        validateCategoryName(name);

        if (!category.getName().equals(name) && categoryRepository.existsByName(name)) {
            throw new EntityAlreadyExistsException(
                    messageProvider.getMessage("category.already_exists", name)
            );
        }

        category.setName(name);
        category.setDescription(description);
        Category updatedCategory = categoryRepository.save(category);
        return categoryConverter.toDto(updatedCategory);
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = getCategoryEntity(id);

        if (categoryRepository.isUsedInRecipes(id)) {
            throw new IllegalStateException(
                    messageProvider.getMessage("category.cannot_delete_used")
            );
        }

        categoryRepository.delete(category);
    }

    @Override
    public boolean isCategoryUsedInRecipes(Long categoryId) {
        return categoryRepository.isUsedInRecipes(categoryId);
    }

    @Override
    public long getRecipeCountByCategory(Long categoryId) {
        return categoryRepository.countRecipesByCategoryId(categoryId);
    }

    @Override
    public List<CategoryDto> getUnusedCategories() {
        return categoryRepository.findUnusedCategories().stream()
                .map(categoryConverter::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryWithUsageDto> getCategoriesWithUsage() {
        return categoryRepository.findAll().stream()
                .map(category -> {
                    long recipeCount = categoryRepository.countRecipesByCategoryId(category.getId());
                    return categoryConverter.toDtoWithUsage(category, recipeCount);
                })
                .collect(Collectors.toList());
    }

    private void validateCategoryName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    messageProvider.getMessage("category.name_empty")
            );
        }
    }

    private void checkCategoryExists(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new EntityAlreadyExistsException(
                    messageProvider.getMessage("category.already_exists", name)
            );
        }
    }

    private Category getCategoryEntity(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("category.not_found", id)
                ));
    }
}