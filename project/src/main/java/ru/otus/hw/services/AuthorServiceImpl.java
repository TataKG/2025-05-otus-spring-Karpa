package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.converters.AuthorConverter;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.exceptions.EntityAlreadyExistsException;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.User;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.UserRepository;
import ru.otus.hw.utils.MessageProvider;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthorServiceImpl implements AuthorService {
    private final AuthorRepository authorRepository;
    private final UserRepository userRepository;
    private final AuthorConverter authorConverter;
    private final MessageProvider messageProvider;

    @Override
    @Transactional
    public AuthorDto createAuthorForUser(Long userId, String bio) {
        try {
            if (authorRepository.findByUserId(userId).isPresent()) {
                throw new EntityAlreadyExistsException(
                        messageProvider.getMessage("author.already_exists")
                );
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            messageProvider.getMessage("user.not_found", userId)
                    ));

            String authorBio = bio != null && !bio.trim().isEmpty() ?
                    bio.trim() : messageProvider.getMessage("author.default_bio");

            Author author = new Author(user, authorBio);
            Author savedAuthor = authorRepository.save(author);

            return authorConverter.toBasicDto(savedAuthor);

        } catch (Exception e) {
            System.err.println("Error creating author for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    @Transactional
    public AuthorDto convertUserToAuthor(Long userId, String bio) {
        return createAuthorForUser(userId, bio);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AuthorDto> getAuthorById(Long id) {
        return authorRepository.findByIdWithUserAndRoles(id)
                .map(authorConverter::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AuthorDto> getAuthorByUserId(Long userId) {
        return authorRepository.findByUserId(userId)
                .map(authorConverter::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AuthorDto> getAuthorByUsername(String username) {
        try {
            Optional<Author> authorOpt = authorRepository.findByUserUsername(username);

            if (authorOpt.isPresent()) {
                Author author = authorOpt.get();
                return Optional.of(authorConverter.toDto(author));
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            System.err.println("Error in getAuthorByUsername: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuthorDto> getAllAuthors() {
        List<Author> authors = authorRepository.findAllWithUser();

        return authors.stream()
                .map(author -> {
                    if (author.getUser() != null && author.getUser().getRoles() != null) {
                        author.getUser().getRoles().size();
                    }
                    return authorConverter.toDto(author);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AuthorDto updateAuthor(Long id, String bio) {
        Author author = authorRepository.findByIdWithUser(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("author.not_found", id)
                ));

        if (bio != null) {
            author.setBio(bio.trim());
        }

        Author updatedAuthor = authorRepository.save(author);

        return authorConverter.toDto(updatedAuthor);
    }

    @Override
    @Transactional
    public void deleteAuthor(Long id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("author.not_found", id)
                ));
        authorRepository.delete(author);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUserId(Long userId) {
        return authorRepository.findByUserId(userId).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AuthorDto> getAuthorWithRecipes(Long id) {
        return authorRepository.findById(id)
                .map(author -> {
                    int recipeCount = author.getRecipes() != null ? author.getRecipes().size() : 0;
                    return authorConverter.toDto(author, recipeCount);
                });
    }
}