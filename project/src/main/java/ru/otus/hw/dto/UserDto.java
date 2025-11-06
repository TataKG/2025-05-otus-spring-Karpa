package ru.otus.hw.dto;

public record UserDto(
        Long id,
        String username,
        String email,
        boolean enabled
) {
}
