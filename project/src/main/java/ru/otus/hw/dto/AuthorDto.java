package ru.otus.hw.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AuthorDto(
        Long id,
        UserDto user,
        String bio,
        LocalDateTime createdAt,
        int recipeCount,
        List<String> roles
) {
    public AuthorDto(Long id, UserDto user, String bio, LocalDateTime createdAt) {
        this(id, user, bio, createdAt, 0, List.of()); // recipeCount = 0 по умолчанию
    }

    public AuthorDto(Long id, UserDto user, String bio, LocalDateTime createdAt, int recipeCount) {
        this(id, user, bio, createdAt, recipeCount, List.of());
    }

}
