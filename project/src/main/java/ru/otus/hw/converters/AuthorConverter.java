package ru.otus.hw.converters;

import org.springframework.stereotype.Component;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.models.Author;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AuthorConverter {

    private final UserConverter userConverter;

    public AuthorConverter(UserConverter userConverter) {
        this.userConverter = userConverter;
    }

    public AuthorDto toDto(Author author) {
        if (author == null) return null;

        return new AuthorDto(
                author.getId(),
                userConverter.toDto(author.getUser()),
                author.getBio(),
                author.getCreatedAt(),
                author.getRecipes() != null ? author.getRecipes().size() : 0
        );
    }

    public List<AuthorDto> toDtoList(List<Author> authors) {
        return authors.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
