package ru.otus.hw.services;

import ru.otus.hw.dto.UserDto;

import java.util.List;
import java.util.Optional;

public interface UserService {
    UserDto createUser(String username, String email, String password, String bio);

    default UserDto createUser(String username, String email, String password) {
        return createUser(username, email, password, null);
    }

    Optional<UserDto> getUserById(Long id);

    Optional<UserDto> getUserByUsername(String username);

    List<UserDto> getAllEnabledUsers();

    boolean userExists(String username);

    boolean emailExists(String email);

    String getUserBio(String username);
}
