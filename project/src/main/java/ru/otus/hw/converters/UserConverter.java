package ru.otus.hw.converters;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.dto.UserDto;
import ru.otus.hw.models.User;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    public List<UserDto> toDtoList(List<User> users) {
        return users.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public UserDto toBasicDto(User user) {
        if (user == null) return null;

        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isEnabled(),
                new HashSet<>(),
                user.getCreatedAt(),
                false
        );
    }
}
