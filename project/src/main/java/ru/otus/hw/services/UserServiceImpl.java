package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.converters.UserConverter;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.UserDto;
import ru.otus.hw.exceptions.EntityAlreadyExistsException;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.User;
import ru.otus.hw.repositories.UserRepository;
import ru.otus.hw.util.MessageProvider;

import java.util.ArrayList;
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

        // Проверяем, есть ли уже пользователи в системе
        boolean isFirstUser = isFirstUserInSystem();

        // Кодируем пароль перед сохранением
        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(username, email, encodedPassword);

        // Определяем роли для пользователя
        if (isFirstUser) {
            user.addRole("ADMIN");
            user.addRole("USER");
            System.out.println("🎉 Первый пользователь создан с ролями: ADMIN, USER");
        } else {
            user.addRole("USER");
            System.out.println("👤 Новый пользователь создан с ролью: USER");
        }

        // Сохраняем пользователя
        User savedUser = userRepository.save(user);

        // Автоматически создаем запись автора с биографией
        createAuthorForUser(savedUser, bio);

        return userConverter.toDto(savedUser);
    }

    /**
     * Проверяет, является ли пользователь первым в системе
     */
    private boolean isFirstUserInSystem() {
        // Используем нативный запрос для точного подсчета
        Long userCount = userRepository.countAllUsers();
        return userCount == 0;
    }

    private void createAuthorForUser(User user, String bio) {
        try {
            // Используем переданную биографию или дефолтную
            String authorBio = (bio != null && !bio.trim().isEmpty()) ? bio.trim() : "Автор кулинарных рецептов";
            AuthorDto authorDto = authorService.createAuthor(user.getId(), authorBio);
            System.out.println("👨‍🍳 Создана запись автора для пользователя: " + user.getUsername() + ", ID: " + authorDto.id());
        } catch (EntityAlreadyExistsException e) {
            System.out.println("👨‍🍳 Запись автора уже существует для пользователя: " + user.getUsername());
        } catch (Exception e) {
            System.err.println("❌ Ошибка при создании автора для пользователя " + user.getUsername() + ": " + e.getMessage());
        }
    }


    private void assignUserRoles(User user) {
        long userCount = userRepository.count();

        if (userCount == 0) {
            user.addRole("ADMIN");
            user.addRole("USER");
            System.out.println("🎉 Первый пользователь создан с ролями: ADMIN, USER");
        } else {
            user.addRole("USER");
            System.out.println("👤 Новый пользователь создан с ролью: USER");
        }
    }

    @Override
    public String getUserBio(String username) {
        return authorService.getAuthorByUsername(username)
                .map(AuthorDto::bio)
                .orElse(null);
    }

    // Остальные методы без изменений...
    @Override
    public Optional<UserDto> getUserById(Long id) {
        return userRepository.findById(id)
                .map(userConverter::toDto);
    }

    @Override
    public Optional<UserDto> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(userConverter::toDto);
    }

    @Override
    public List<UserDto> getAllEnabledUsers() {
        return userRepository.findAllEnabledUsers().stream()
                .map(userConverter::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public boolean userExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}