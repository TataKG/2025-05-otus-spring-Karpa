package ru.otus.hw.converters;

import org.springframework.stereotype.Component;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.models.Author;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AuthorConverter {

    private final UserConverter userConverter;

    public AuthorConverter(UserConverter userConverter) {
        this.userConverter = userConverter;
    }

    public AuthorDto toDto(Author author) {
        if (author == null) {
            return null;
        }

        // Получаем роли пользователя напрямую из User
        Set<String> roles = author.getUser().getRoles();

        return new AuthorDto(
                author.getId(),
                userConverter.toDto(author.getUser()),
                author.getBio(),
                author.getCreatedAt(),
                author.getRecipes().size(), // Добавить подсчет рецептов
                new ArrayList<>(roles)
        );
    }

    public AuthorDto toDto(Author author, int recipeCount) {
        if (author == null) {
            return null;
        }

        // Получаем роли пользователя напрямую из User
        Set<String> roles = author.getUser().getRoles();

        return new AuthorDto(
                author.getId(),
                userConverter.toDto(author.getUser()),
                author.getBio(),
                author.getCreatedAt(),
                recipeCount,
                new ArrayList<>(roles) // Конвертируем Set в List для DTO
        );
    }
}