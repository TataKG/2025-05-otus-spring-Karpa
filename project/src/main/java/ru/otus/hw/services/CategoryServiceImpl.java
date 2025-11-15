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
import ru.otus.hw.utils.MessageProvider;

import java.util.List;
import java.util.Map;
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
    @Transactional
    public CategoryDto createCategory(String name, String description) {
        validateCategoryName(name);
        String trimmedName = name.trim();

        checkCategoryExists(trimmedName);

        Category category = new Category(trimmedName, description != null ? description.trim() : null);
        Category savedCategory = categoryRepository.save(category);
        return categoryConverter.toDto(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CategoryDto> getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(categoryConverter::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CategoryDto> getCategoryEntityForInternalUse(Long id) {
        return categoryRepository.findById(id)
                .map(category -> {
                    if (category.getRecipes() != null) {
                        category.getRecipes().size();
                    }
                    return categoryConverter.toDto(category);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CategoryDto> getCategoryByName(String name) {
        return categoryRepository.findByName(name)
                .map(categoryConverter::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryConverter::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
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
    @Transactional
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
    @Transactional(readOnly = true)
    public boolean isCategoryUsedInRecipes(Long categoryId) {
        return categoryRepository.isUsedInRecipes(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getRecipeCountByCategory(Long categoryId) {
        return categoryRepository.countRecipesByCategoryId(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getUnusedCategories() {
        return categoryRepository.findUnusedCategories().stream()
                .map(categoryConverter::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryWithUsageDto> getCategoriesWithUsage() {
        List<Object[]> results = categoryRepository.findAllWithRecipeCount();

        return results.stream()
                .map(result -> {
                    Category category = (Category) result[0];
                    Long recipeCount = (Long) result[1];
                    return categoryConverter.toDtoWithUsage(category, recipeCount);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryWithUsageDto> getCategoriesWithPublishedUsage() {
        List<Object[]> results = categoryRepository.findAllWithPublishedRecipeCount();

        return results.stream()
                .map(result -> {
                    Category category = (Category) result[0];
                    Long publishedRecipeCount = (Long) result[1];
                    return categoryConverter.toDtoWithUsage(category, publishedRecipeCount);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategoriesWithRecipes() {
        return categoryRepository.findAllWithRecipes().stream()
                .map(categoryConverter::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Boolean> getCategoriesUsageStatus(List<Long> categoryIds) {
        return categoryIds.stream()
                .collect(Collectors.toMap(
                        categoryId -> categoryId,
                        categoryRepository::isUsedInRecipes
                ));
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