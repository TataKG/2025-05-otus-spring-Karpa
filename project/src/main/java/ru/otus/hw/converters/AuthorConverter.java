package ru.otus.hw.converters;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.UserDto;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class AuthorConverter {

    private final UserConverter userConverter;

    @Transactional(readOnly = true)
    public AuthorDto toDto(Author author) {
        if (author == null) {
            return null;
        }

        User user = author.getUser();
        if (user == null) {
            throw new IllegalStateException("User should be loaded for author with ID: " + author.getId());
        }

        Set<String> roles = user.getRoles() != null ? user.getRoles() : new HashSet<>();
        int recipeCount = author.getRecipes() != null ? author.getRecipes().size() : 0;

        return new AuthorDto(
                author.getId(),
                userConverter.toDto(user),
                author.getBio(),
                author.getCreatedAt(),
                recipeCount,
                new ArrayList<>(roles)
        );
    }

    public AuthorDto toDto(Author author, int recipeCount) {
        if (author == null) {
            return null;
        }

        User user = author.getUser();
        if (user == null) {
            throw new IllegalStateException("User should be loaded for author with ID: " + author.getId());
        }

        Set<String> roles = user.getRoles() != null ? user.getRoles() : new HashSet<>();

        return new AuthorDto(
                author.getId(),
                userConverter.toDto(user),
                author.getBio(),
                author.getCreatedAt(),
                recipeCount,
                new ArrayList<>(roles)
        );
    }

    public AuthorDto toBasicDto(Author author) {
        if (author == null) {
            return null;
        }

        User user = author.getUser();
        if (user == null) {
            return new AuthorDto(
                    author.getId(),
                    null,
                    author.getBio(),
                    author.getCreatedAt(),
                    0,
                    new ArrayList<>()
            );
        }

        UserDto userDto = new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isEnabled(),
                new HashSet<>(),
                user.getCreatedAt(),
                true
        );

        return new AuthorDto(
                author.getId(),
                userDto,
                author.getBio(),
                author.getCreatedAt(),
                0,
                new ArrayList<>()
        );
    }
}