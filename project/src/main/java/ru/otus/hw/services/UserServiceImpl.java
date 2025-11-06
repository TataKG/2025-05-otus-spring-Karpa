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
import ru.otus.hw.dto.UserDto;
import ru.otus.hw.exceptions.EntityAlreadyExistsException;
import ru.otus.hw.exceptions.EntityNotFoundException;
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

    @Override
    public UserDto createUser(String username, String email, String password) {
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

        // Кодируем пароль перед сохранением
        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(username, email, encodedPassword);
        User savedUser = userRepository.save(user);
        return userConverter.toDto(savedUser);
    }

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