package ru.otus.hw.converters;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.models.Author;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class AuthorConverter {

    private final UserConverter userConverter;

    public AuthorDto toDto(Author author) {
        if (author == null) {
            return null;
        }

        Set<String> roles = author.getUser() != null ? author.getUser().getRoles() : new HashSet<>();

        return new AuthorDto(
                author.getId(),
                userConverter.toDto(author.getUser()),
                author.getBio(),
                author.getCreatedAt(),
                author.getRecipes() != null ? author.getRecipes().size() : 0,
                new ArrayList<>(roles)
        );
    }

    public AuthorDto toDto(Author author, int recipeCount) {
        if (author == null) {
            return null;
        }

        Set<String> roles = author.getUser().getRoles();

        return new AuthorDto(
                author.getId(),
                userConverter.toDto(author.getUser()),
                author.getBio(),
                author.getCreatedAt(),
                recipeCount,
                new ArrayList<>(roles)
        );
    }
}