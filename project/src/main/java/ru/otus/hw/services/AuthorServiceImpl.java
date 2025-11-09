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

//    public AuthorServiceImpl(AuthorRepository authorRepository,
//                             UserRepository userRepository,
//                             AuthorConverter authorConverter,
//                             MessageProvider messageProvider) {
//        this.authorRepository = authorRepository;
//        this.userRepository = userRepository;
//        this.authorConverter = authorConverter;
//        this.messageProvider = messageProvider;
//    }

    @Override
    public AuthorDto createAuthor(Long userId, String bio) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("user.not_found", userId)
                ));

        if (authorRepository.findByUser(user).isPresent()) {
            throw new EntityAlreadyExistsException(
                    messageProvider.getMessage("author.already_exists")
            );
        }

        Author author = new Author(user, bio);
        Author savedAuthor = authorRepository.save(author);
        return authorConverter.toDto(savedAuthor);
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
        System.out.println("Searching author by username: " + username); // Логирование

        Optional<Author> authorOpt = authorRepository.findByUserUsername(username);

        if (authorOpt.isPresent()) {
            AuthorDto authorDto = authorConverter.toDto(authorOpt.get());
            System.out.println("Found author: " + authorDto.id() + " for username: " + username);
            return Optional.of(authorDto);
        } else {
            System.out.println("No author found for username: " + username);

            // Дополнительная проверка - существует ли пользователь
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                System.out.println("But user exists: " + userOpt.get().getId());
            } else {
                System.out.println("User not found: " + username);
            }

            return Optional.empty();
        }
    }

    @Override
    public List<AuthorDto> getAllAuthors() {
        // Используем метод с загрузкой ролей
        return authorRepository.findAllWithUserAndRoles().stream()
                .map(authorConverter::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public AuthorDto convertUserToAuthor(Long userId, String bio) {
        return createAuthor(userId, bio);
    }
}
