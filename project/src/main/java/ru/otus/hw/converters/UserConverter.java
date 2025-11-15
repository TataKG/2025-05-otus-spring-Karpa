package ru.otus.hw.converters;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.dto.UserDto;
import ru.otus.hw.models.User;

import java.util.HashSet;
import java.util.Set;

@Component
public class UserConverter {

    @Transactional(readOnly = true)
    public UserDto toDto(User user) {
        if (user == null) return null;

        Set<String> roles = user.getRoles() != null ? user.getRoles() : new HashSet<>();

        boolean isAuthor = user.getAuthor() != null;

        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isEnabled(),
                roles,
                user.getCreatedAt(),
                isAuthor
        );
    }
}
