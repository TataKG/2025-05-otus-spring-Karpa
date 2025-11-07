package ru.otus.hw.services;

import ru.otus.hw.dto.AuthorDto;

import java.util.List;
import java.util.Optional;

public interface AuthorService {
    AuthorDto createAuthor(Long userId, String bio);

    Optional<AuthorDto> getAuthorById(Long id);

    Optional<AuthorDto> getAuthorByUserId(Long userId);

    Optional<AuthorDto> getAuthorByUsername(String username);

    List<AuthorDto> getAllAuthors();

    AuthorDto convertUserToAuthor(Long userId, String bio);
}
