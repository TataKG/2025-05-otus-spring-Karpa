package ru.otus.hw.dto;

import java.time.LocalDateTime;

public record AuthorDto(
        Long id,
        UserDto user,
        String bio,
        LocalDateTime createdAt,
        int recipeCount
) {
    public AuthorDto(Long id, UserDto user, String bio, LocalDateTime createdAt) {
        this(id, user, bio, createdAt, 0); // recipeCount = 0 по умолчанию
    }
}
