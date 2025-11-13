package ru.otus.hw.services;

import ru.otus.hw.dto.AuthorDto;

import java.util.List;
import java.util.Optional;

public interface AuthorService {
    AuthorDto createAuthorForUser(Long userId, String bio);

    AuthorDto convertUserToAuthor(Long userId, String bio);

    Optional<AuthorDto> getAuthorById(Long id);

    Optional<AuthorDto> getAuthorForInternalUse(Long id); // переименовать!

    Optional<AuthorDto> getAuthorByUserId(Long userId);

    Optional<AuthorDto> getAuthorByUsername(String username);

    List<AuthorDto> getAllAuthors();

    AuthorDto updateAuthor(Long id, String bio);

    void deleteAuthor(Long id);

    boolean existsByUserId(Long userId);

    Optional<AuthorDto> getAuthorWithRecipes(Long id);
}