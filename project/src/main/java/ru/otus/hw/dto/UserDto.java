package ru.otus.hw.dto;

import ru.otus.hw.models.User;

import java.time.LocalDateTime;
import java.util.Set;

public record UserDto(
        Long id,
        String username,
        String email,
        boolean enabled,
        Set<String> roles,
        LocalDateTime createdAt,
        boolean isAuthor
) {
    public static UserDto fromEntity(User user) {
        if (user == null) {
            return null;
        }

        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isEnabled(),
                user.getRoles(),
                user.getCreatedAt(),
                user.getAuthor() != null
        );
    }
}
