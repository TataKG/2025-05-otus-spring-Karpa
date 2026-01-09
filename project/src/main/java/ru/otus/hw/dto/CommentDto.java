package ru.otus.hw.dto;

import java.time.LocalDateTime;

public record CommentDto(
        Long id,
        String content,
        UserDto user,
        Long recipeId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean canEdit,
        boolean canDelete
) {
}
