package ru.otus.hw.dto;

import ru.otus.hw.util.MessageProvider;

import java.time.LocalDateTime;

public record CategoryWithUsageDto(Long id, String name, String description, LocalDateTime createdAt,
                                   boolean usedInRecipes, long recipeCount, long publishedRecipeCount,
                                   boolean canBeDeleted) {

    public CategoryWithUsageDto {
        canBeDeleted = (recipeCount == 0);
    }

    public CategoryWithUsageDto(Long id, String name, String description, LocalDateTime createdAt, boolean usedInRecipes, long recipeCount) {
        this(id, name, description, createdAt, usedInRecipes, recipeCount, 0, recipeCount == 0);
    }

    public static CategoryWithUsageDto from(CategoryDto category, long recipeCount, long publishedRecipeCount) {
        return new CategoryWithUsageDto(category.id(), category.name(), category.description(), category.createdAt(), recipeCount > 0, recipeCount, publishedRecipeCount, recipeCount == 0);
    }

    public String getUsageStatus(MessageProvider messageProvider) {
        if (recipeCount == 0) {
            return messageProvider.getMessage("category.status.not_used");
        } else if (publishedRecipeCount > 0) {
            return messageProvider.getMessage("category.status.used_with_published", recipeCount, publishedRecipeCount);
        } else {
            return messageProvider.getMessage("category.status.used_only_drafts", recipeCount);
        }
    }

    public String getStatusColor() {
        if (recipeCount == 0) {
            return "success";
        } else if (publishedRecipeCount > 0) {
            return "danger";
        } else {
            return "warning";
        }
    }

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