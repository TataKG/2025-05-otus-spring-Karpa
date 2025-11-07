package ru.otus.hw.converters;

import org.springframework.stereotype.Component;
import ru.otus.hw.dto.CategoryDto;
import ru.otus.hw.dto.CategoryWithUsageDto;
import ru.otus.hw.models.Category;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CategoryConverter {

    public CategoryDto toDto(Category category) {
        if (category == null) return null;

        return new CategoryDto(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getCreatedAt()
        );
    }

    public List<CategoryDto> toDtoList(List<Category> categories) {
        return categories.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public CategoryWithUsageDto toDtoWithUsage(Category category, long recipeCount, long publishedRecipeCount) {
        return new CategoryWithUsageDto(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getCreatedAt(),
                recipeCount > 0,
                recipeCount,
                publishedRecipeCount,
                recipeCount == 0
        );
    }

    /**
     * Конвертировать в DTO с информацией об использовании (упрощенный вариант)
     */
    public CategoryWithUsageDto toDtoWithUsage(Category category, long recipeCount) {
        return new CategoryWithUsageDto(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getCreatedAt(),
                recipeCount > 0,
                recipeCount
        );
    }

    /**
     * Конвертировать из CategoryDto в CategoryWithUsageDto
     */
    public CategoryWithUsageDto toDtoWithUsage(CategoryDto categoryDto, long recipeCount, long publishedRecipeCount) {
        return new CategoryWithUsageDto(
                categoryDto.id(),
                categoryDto.name(),
                categoryDto.description(),
                categoryDto.createdAt(),
                recipeCount > 0,
                recipeCount,
                publishedRecipeCount,
                recipeCount == 0
        );
    }

    /**
     * Конвертировать список категорий с информацией об использовании
     */
    public List<CategoryWithUsageDto> toDtoWithUsageList(List<Category> categories,
                                                         Map<Long, Long> recipeCounts,
                                                         Map<Long, Long> publishedRecipeCounts) {
        return categories.stream()
                .map(category -> {
                    Long categoryId = category.getId();
                    long recipeCount = recipeCounts.getOrDefault(categoryId, 0L);
                    long publishedRecipeCount = publishedRecipeCounts.getOrDefault(categoryId, 0L);

                    return toDtoWithUsage(category, recipeCount, publishedRecipeCount);
                })
                .collect(Collectors.toList());
    }
}

