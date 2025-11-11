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
}
