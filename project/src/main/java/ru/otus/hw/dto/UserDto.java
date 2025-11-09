package ru.otus.hw.dto;

import ru.otus.hw.models.User;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public record UserDto(
        Long id,
        String username,
        String email,
        boolean enabled,
        Set<String> roles,
        LocalDateTime createdAt,
        boolean isAuthor
) {
    public UserDto(Long id, String username, String email, boolean enabled, Set<String> roles, LocalDateTime createdAt) {
        this(id, username, email, enabled, roles, createdAt, false); // isAuthor = false по умолчанию
    }

    // Или конструктор с 4 параметрами для простых случаев
    public UserDto(Long id, String username, String email, Set<String> roles) {
        this(id, username, email, true, roles, LocalDateTime.now(), false);
    }

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
