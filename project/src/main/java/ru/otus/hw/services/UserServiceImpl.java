package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.converters.UserConverter;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.UserDto;
import ru.otus.hw.exceptions.EntityAlreadyExistsException;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.User;
import ru.otus.hw.repositories.UserRepository;
import ru.otus.hw.util.MessageProvider;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserConverter userConverter;
    private final MessageProvider messageProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuthorService authorService;

    @Override
    @Transactional
    public UserDto createUser(String username, String email, String password, String bio) {
        if (userExists(username)) {
            throw new EntityAlreadyExistsException(
                    messageProvider.getMessage("user.already_exists.username", username)
            );
        }
        if (emailExists(email)) {
            throw new EntityAlreadyExistsException(
                    messageProvider.getMessage("user.already_exists.email", email)
            );
        }

        boolean isFirstUser = isFirstUserInSystem();
        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(username, email, encodedPassword);

        if (isFirstUser) {
            user.addRole("ADMIN");
        }

        User savedUser = userRepository.save(user);

        authorService.createAuthorForUser(savedUser.getId(), bio);

        return userRepository.findByIdWithRolesAndAuthor(savedUser.getId())
                .map(userConverter::toDto)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("user.not_found", savedUser.getId())
                ));
    }

    private boolean isFirstUserInSystem() {
        return userRepository.count() == 0;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> getUserById(Long id) {
        return userRepository.findByIdWithRolesAndAuthor(id)
                .map(userConverter::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> getUserByUsername(String username) {
        return userRepository.findByUsernameWithRoles(username)
                .map(userConverter::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllEnabledUsers() {
        return userRepository.findAllEnabledUsers().stream()
                .map(userConverter::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean userExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public String getUserBio(String username) {
        return authorService.getAuthorByUsername(username)
                .map(AuthorDto::bio)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> getUserWithAuthorAndRoles(Long id) {
        return userRepository.findByIdWithRolesAndAuthor(id)
                .map(userConverter::toDto);
    }
}