package ru.otus.hw.converters;

import org.springframework.stereotype.Component;
import ru.otus.hw.dto.CategoryDto;
import ru.otus.hw.dto.CategoryWithUsageDto;
import ru.otus.hw.models.Category;

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

    public CategoryWithUsageDto toDtoWithUsage(Category category, long recipeCount) {
        if (category == null) return null;

        return new CategoryWithUsageDto(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getCreatedAt(),
                recipeCount > 0,
                recipeCount
        );
    }

}

