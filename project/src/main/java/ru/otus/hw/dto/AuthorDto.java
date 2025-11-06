package ru.otus.hw.dto;

public record AuthorDto(
        Long id,
        UserDto user,
        String bio,
        int recipeCount
) {
}
