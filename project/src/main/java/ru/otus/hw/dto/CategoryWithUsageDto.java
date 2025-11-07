package ru.otus.hw.dto;

import java.time.LocalDateTime;

public record CategoryWithUsageDto(
        Long id,
        String name,
        String description,
        LocalDateTime createdAt,
        boolean usedInRecipes,
        long recipeCount,
        long publishedRecipeCount,
        boolean canBeDeleted
) {

    /**
     * Конструктор с автоматическим вычислением canBeDeleted
     */
    public CategoryWithUsageDto {
        // Категорию можно удалить только если в ней нет рецептов
        canBeDeleted = (recipeCount == 0);
    }

    /**
     * Упрощенный конструктор без publishedRecipeCount
     */
    public CategoryWithUsageDto(Long id, String name, String description,
                                LocalDateTime createdAt, boolean usedInRecipes,
                                long recipeCount) {
        this(id, name, description, createdAt, usedInRecipes, recipeCount, 0, recipeCount == 0);
    }

    /**
     * Конструктор из Entity и счетчиков
     */
    public static CategoryWithUsageDto from(CategoryDto category, long recipeCount, long publishedRecipeCount) {
        return new CategoryWithUsageDto(
                category.id(),
                category.name(),
                category.description(),
                category.createdAt(),
                recipeCount > 0,
                recipeCount,
                publishedRecipeCount,
                recipeCount == 0
        );
    }

    /**
     * Получить статус использования в виде текста
     */
    public String getUsageStatus() {
        if (recipeCount == 0) {
            return "Не используется";
        } else if (publishedRecipeCount > 0) {
            return String.format("Используется в %d рецептах (%d опубликовано)", recipeCount, publishedRecipeCount);
        } else {
            return String.format("Используется в %d рецептах (все черновики)", recipeCount);
        }
    }

    /**
     * Получить цвет статуса для UI
     */
    public String getStatusColor() {
        if (recipeCount == 0) {
            return "success"; // зеленый - можно удалять
        } else if (publishedRecipeCount > 0) {
            return "danger"; // красный - есть опубликованные рецепты
        } else {
            return "warning"; // желтый - есть только черновики
        }
    }

    /**
     * Получить иконку статуса
     */
    public String getStatusIcon() {
        if (recipeCount == 0) {
            return "✅";
        } else if (publishedRecipeCount > 0) {
            return "❌";
        } else {
            return "⚠️";
        }
    }
}
