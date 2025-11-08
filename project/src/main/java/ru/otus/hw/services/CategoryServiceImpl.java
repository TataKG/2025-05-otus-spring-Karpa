package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.converters.CategoryConverter;
import ru.otus.hw.dto.CategoryDto;
import ru.otus.hw.dto.CategoryWithUsageDto;
import ru.otus.hw.exceptions.EntityAlreadyExistsException;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.exceptions.ValidationException;
import ru.otus.hw.models.Category;
import ru.otus.hw.models.Recipe;
import ru.otus.hw.repositories.CategoryRepository;
import ru.otus.hw.repositories.RecipeRepository;
import ru.otus.hw.util.MessageProvider;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryConverter categoryConverter;
    private final RecipeRepository recipeRepository;
    private final MessageProvider messageProvider;

    @Override
    public CategoryDto createCategory(String name) {
        if (categoryExists(name)) {
            throw new EntityAlreadyExistsException(
                    messageProvider.getMessage("category.already_exists", name)
            );
        }

        Category category = new Category(name);
        Category savedCategory = categoryRepository.save(category);
        return categoryConverter.toDto(savedCategory);
    }

    @Override
    public Optional<CategoryDto> getCategoryById(Long id) {
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
        return StreamSupport.stream(categoryRepository.findAll().spliterator(), false)
                .map(categoryConverter::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<CategoryDto> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable)
                .map(categoryConverter::toDto);
    }

    @Override
    public List<CategoryDto> findCategoriesByNameContaining(String name) {
        return categoryRepository.findByNameContainingIgnoreCase(name).stream()
                .map(categoryConverter::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public boolean categoryExists(String name) {
        return categoryRepository.existsByName(name);
    }

    @Override
    public CategoryDto createCategoryWithDescription(String name, String description) {
        System.out.println("Creating category with name: '" + name + "', description: '" + description + "'");

        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Название категории не может быть пустым");
        }

        String trimmedName = name.trim();

        // Проверяем существование категории
        boolean exists = categoryRepository.existsByName(trimmedName);
        System.out.println("Category exists check for '" + trimmedName + "': " + exists);

        if (exists) {
            throw new EntityAlreadyExistsException(
                    messageProvider.getMessage("category.already_exists", trimmedName)
            );
        }

        Category category = new Category(trimmedName, description != null ? description.trim() : null);
        Category savedCategory = categoryRepository.save(category);
        System.out.println("Category saved with ID: " + savedCategory.getId());

        return categoryConverter.toDto(savedCategory);
    }

    @Override
    public CategoryDto updateCategory(Long id, String name, String description) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("category.not_found", id)
                ));

        // Проверяем уникальность имени (если изменилось)
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
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("category.not_found", id)
                ));

        // Проверяем, используется ли категория в рецептах
        if (categoryRepository.isUsedInRecipes(id)) {
            throw new IllegalStateException(
                    messageProvider.getMessage("category.cannot_delete_used")
            );
        }

        try {
            categoryRepository.delete(category);
        } catch (Exception e) {
            throw new IllegalStateException("Не удалось удалить категорию: " + e.getMessage());
        }
    }

    @Override
    public boolean isCategoryUsedInRecipes(Long categoryId) {
        return categoryRepository.isUsedInRecipes(categoryId);
    }

    @Override
    public long getRecipeCountByCategory(Long categoryId) {
        return categoryRepository.countAllRecipesByCategoryId(categoryId);
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
        List<Category> categories = categoryRepository.findAll();

        // Получаем статистику использования для всех категорий
        Map<Long, Long> recipeCounts = categories.stream()
                .collect(Collectors.toMap(
                        Category::getId,
                        category -> categoryRepository.countAllRecipesByCategoryId(category.getId())
                ));

        Map<Long, Long> publishedRecipeCounts = categories.stream()
                .collect(Collectors.toMap(
                        Category::getId,
                        category -> categoryRepository.countPublishedRecipesByCategoryId(category.getId())
                ));

        return categories.stream()
                .map(category -> {
                    Long categoryId = category.getId();
                    long recipeCount = recipeCounts.getOrDefault(categoryId, 0L);
                    long publishedRecipeCount = publishedRecipeCounts.getOrDefault(categoryId, 0L);

                    return categoryConverter.toDtoWithUsage(category, recipeCount, publishedRecipeCount);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CategoryWithUsageDto> getCategoryWithUsageById(Long id) {
        return categoryRepository.findById(id)
                .map(category -> {
                    long recipeCount = categoryRepository.countAllRecipesByCategoryId(id);
                    long publishedRecipeCount = categoryRepository.countPublishedRecipesByCategoryId(id);

                    return categoryConverter.toDtoWithUsage(category, recipeCount, publishedRecipeCount);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public long getPublishedRecipeCountByCategory(Long categoryId) {
        return categoryRepository.countPublishedRecipesByCategoryId(categoryId);
    }

}
