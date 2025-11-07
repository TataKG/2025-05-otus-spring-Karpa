package ru.otus.hw.converters;

import org.springframework.stereotype.Component;
import ru.otus.hw.dto.UserDto;
import ru.otus.hw.models.User;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserConverter {

    public UserDto toDto(User user) {
        if (user == null) return null;

        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isEnabled(),
                user.getRoles() != null ? user.getRoles() : new HashSet<>(),
                user.getCreatedAt(),
                user.isAuthor()
        );
    }

    public List<UserDto> toDtoList(List<User> users) {
        return users.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
