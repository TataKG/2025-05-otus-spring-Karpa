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
import ru.otus.hw.util.MessageProvider;

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
    public AuthorDto createAuthorForUser(Long userId, String bio) {
        try {
            System.out.println("Creating author for user ID: " + userId + ", bio: " + bio);

            // Проверяем, существует ли автор
            if (authorRepository.findByUserId(userId).isPresent()) {
                System.err.println("Author already exists for user ID: " + userId);
                throw new EntityAlreadyExistsException(
                        messageProvider.getMessage("author.already_exists")
                );
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        System.err.println("User not found with ID: " + userId);
                        return new EntityNotFoundException(
                                messageProvider.getMessage("user.not_found", userId)
                        );
                    });

            String authorBio = bio != null && !bio.trim().isEmpty() ?
                    bio.trim() : messageProvider.getMessage("author.default_bio");

            System.out.println("Creating new author for user: " + user.getUsername());

            Author author = new Author(user, authorBio);
            Author savedAuthor = authorRepository.save(author);

            System.out.println("Author created successfully with ID: " + savedAuthor.getId());

            return authorConverter.toDto(savedAuthor);
        } catch (Exception e) {
            System.err.println("Error creating author for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public Optional<AuthorDto> getAuthorForInternalUse(Long id) {
        return authorRepository.findByIdWithUser(id)
                .map(authorConverter::toDto);
    }

    @Override
    public AuthorDto convertUserToAuthor(Long userId, String bio) {
        return createAuthorForUser(userId, bio);
    }

    @Override
    public Optional<AuthorDto> getAuthorById(Long id) {
        return authorRepository.findByIdWithUser(id)
                .map(authorConverter::toDto);
    }

    @Override
    public Optional<AuthorDto> getAuthorByUserId(Long userId) {
        return authorRepository.findByUserId(userId)
                .map(authorConverter::toDto);
    }

    @Override
    public Optional<AuthorDto> getAuthorByUsername(String username) {
        try {
            System.out.println("Searching author by username: " + username); // Логирование

            Optional<Author> authorOpt = authorRepository.findByUserUsername(username);

            if (authorOpt.isPresent()) {
                Author author = authorOpt.get();
                System.out.println("Found author: " + author.getId() + " for username: " + username);
                return Optional.of(authorConverter.toDto(author));
            } else {
                System.err.println("Author not found for username: " + username);
                return Optional.empty();
            }
        } catch (Exception e) {
            System.err.println("Error in getAuthorByUsername: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public List<AuthorDto> getAllAuthors() {
        return authorRepository.findAllWithUserAndRoles().stream()
                .map(authorConverter::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public AuthorDto updateAuthor(Long id, String bio) {
        Author author = authorRepository.findById(id)
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
    public void deleteAuthor(Long id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("author.not_found", id)
                ));
        authorRepository.delete(author);
    }

    @Override
    public boolean existsByUserId(Long userId) {
        return authorRepository.findByUserId(userId).isPresent();
    }
}