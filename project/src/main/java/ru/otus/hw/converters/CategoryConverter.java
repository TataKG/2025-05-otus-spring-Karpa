package ru.otus.hw.converters;

import org.springframework.stereotype.Component;
import ru.otus.hw.dto.CategoryDto;
import ru.otus.hw.dto.CategoryWithRecipesDto;
import ru.otus.hw.dto.CategoryWithUsageDto;
import ru.otus.hw.dto.RecipeInfoDto;
import ru.otus.hw.models.Category;

import java.util.ArrayList;
import java.util.List;
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

    public CategoryWithUsageDto toDtoWithUsage(Object[] result) {
        if (result == null || result.length < 2) return null;

        Category category = (Category) result[0];
        Long recipeCount = (Long) result[1];

        return new CategoryWithUsageDto(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getCreatedAt(),
                recipeCount > 0,
                recipeCount
        );
    }

    public CategoryWithRecipesDto toDtoWithRecipes(Category category) {
        if (category == null) return null;

        List<RecipeInfoDto> recipeInfos = category.getRecipes() != null ?
                category.getRecipes().stream()
                        .map(recipe -> new RecipeInfoDto(
                                recipe.getId(),
                                recipe.getTitle(),
                                recipe.isPublished(),
                                recipe.getCreatedAt()
                        ))
                        .collect(Collectors.toList()) :
                new ArrayList<>();

        return new CategoryWithRecipesDto(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getCreatedAt(),
                recipeInfos
        );
    }
}

