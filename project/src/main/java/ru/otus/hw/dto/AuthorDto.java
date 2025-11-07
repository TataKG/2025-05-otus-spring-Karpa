package ru.otus.hw.dto;

import java.time.LocalDateTime;

public record AuthorDto(
        Long id,
        UserDto user,
        String bio,
        LocalDateTime createdAt,
        int recipeCount
) {
}
