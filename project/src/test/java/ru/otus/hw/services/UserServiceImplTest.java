package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.otus.hw.converters.UserConverter;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.UserDto;
import ru.otus.hw.exceptions.EntityAlreadyExistsException;
import ru.otus.hw.models.User;
import ru.otus.hw.repositories.UserRepository;
import ru.otus.hw.util.MessageProvider;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты для UserServiceImpl")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserConverter userConverter;

    @Mock
    private MessageProvider messageProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthorService authorService;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserDto testUserDto;
    private AuthorDto testAuthorDto;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "encodedPassword");
        testUser.setId(1L);
        testUser.addRole("USER");

        testUserDto = new UserDto(1L, "testuser", "test@example.com", true,
                Set.of("USER"), LocalDateTime.now(), true);

        testAuthorDto = new AuthorDto(1L, testUserDto, "Test bio",
                LocalDateTime.now(), 5, List.of("USER"));
    }

    @Test
    @DisplayName("Создание пользователя - успешное создание")
    void createUser_ShouldCreateUser_WhenValidData() {
        // Given
        String username = "testuser";
        String email = "test@example.com";
        String password = "password";
        String bio = "Test bio";

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userRepository.findByIdWithRolesAndAuthor(1L)).thenReturn(Optional.of(testUser));
        when(userConverter.toDto(testUser)).thenReturn(testUserDto);

        // When
        UserDto result = userService.createUser(username, email, password, bio);

        // Then
        assertNotNull(result);
        assertEquals(testUserDto, result);
        verify(userRepository).save(any(User.class));
        verify(authorService).createAuthorForUser(1L, bio);
    }

    @Test
    @DisplayName("Создание пользователя - первый пользователь становится админом")
    void createUser_ShouldMakeFirstUserAdmin_WhenFirstUser() {
        // Given
        String username = "admin";
        String email = "admin@example.com";
        String password = "password";
        String bio = "Admin bio";

        User adminUser = new User("admin", "admin@example.com", "encodedPassword");
        adminUser.setId(1L);
        adminUser.addRole("ADMIN");
        adminUser.addRole("USER");

        UserDto adminUserDto = new UserDto(1L, "admin", "admin@example.com", true,
                Set.of("ADMIN", "USER"), LocalDateTime.now(), true);

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(adminUser);
        when(userRepository.findByIdWithRolesAndAuthor(1L)).thenReturn(Optional.of(adminUser));
        when(userConverter.toDto(adminUser)).thenReturn(adminUserDto);

        // When
        UserDto result = userService.createUser(username, email, password, bio);

        // Then
        assertNotNull(result);
        assertTrue(result.roles().contains("ADMIN"));
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Создание пользователя - имя пользователя уже существует")
    void createUser_ShouldThrowException_WhenUsernameExists() {
        // Given
        String username = "testuser";
        String email = "test@example.com";
        String password = "password";
        String bio = "Test bio";

        when(userRepository.existsByUsername(username)).thenReturn(true);
        when(messageProvider.getMessage("user.already_exists.username", username))
                .thenReturn("Username already exists");

        // When & Then
        assertThrows(EntityAlreadyExistsException.class,
                () -> userService.createUser(username, email, password, bio));
    }

    @Test
    @DisplayName("Создание пользователя - email уже существует")
    void createUser_ShouldThrowException_WhenEmailExists() {
        // Given
        String username = "testuser";
        String email = "test@example.com";
        String password = "password";
        String bio = "Test bio";

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(true);
        when(messageProvider.getMessage("user.already_exists.email", email))
                .thenReturn("Email already exists");

        // When & Then
        assertThrows(EntityAlreadyExistsException.class,
                () -> userService.createUser(username, email, password, bio));
    }

    @Test
    @DisplayName("Получение пользователя по ID - пользователь найден")
    void getUserById_ShouldReturnUser_WhenUserExists() {
        // Given
        Long userId = 1L;
        when(userRepository.findByIdWithRolesAndAuthor(userId)).thenReturn(Optional.of(testUser));
        when(userConverter.toDto(testUser)).thenReturn(testUserDto);

        // When
        Optional<UserDto> result = userService.getUserById(userId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testUserDto, result.get());
    }

    @Test
    @DisplayName("Получение пользователя по ID - пользователь не найден")
    void getUserById_ShouldReturnEmpty_WhenUserNotExists() {
        // Given
        Long userId = 1L;
        when(userRepository.findByIdWithRolesAndAuthor(userId)).thenReturn(Optional.empty());

        // When
        Optional<UserDto> result = userService.getUserById(userId);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Получение пользователя по имени пользователя - пользователь найден")
    void getUserByUsername_ShouldReturnUser_WhenUserExists() {
        // Given
        String username = "testuser";
        when(userRepository.findByUsernameWithRoles(username)).thenReturn(Optional.of(testUser));
        when(userConverter.toDto(testUser)).thenReturn(testUserDto);

        // When
        Optional<UserDto> result = userService.getUserByUsername(username);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testUserDto, result.get());
    }

    @Test
    @DisplayName("Получение всех активных пользователей - успешно")
    void getAllEnabledUsers_ShouldReturnEnabledUsers() {
        // Given
        List<User> users = List.of(testUser);
        when(userRepository.findAllEnabledUsers()).thenReturn(users);
        when(userConverter.toDto(testUser)).thenReturn(testUserDto);

        // When
        List<UserDto> result = userService.getAllEnabledUsers();

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(testUserDto, result.get(0));
    }

    @Test
    @DisplayName("Проверка существования пользователя по имени - существует")
    void userExists_ShouldReturnTrue_WhenUserExists() {
        // Given
        String username = "testuser";
        when(userRepository.existsByUsername(username)).thenReturn(true);

        // When
        boolean result = userService.userExists(username);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("Проверка существования email - существует")
    void emailExists_ShouldReturnTrue_WhenEmailExists() {
        // Given
        String email = "test@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // When
        boolean result = userService.emailExists(email);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("Получение биографии пользователя - успешно")
    void getUserBio_ShouldReturnBio_WhenAuthorExists() {
        // Given
        String username = "testuser";
        when(authorService.getAuthorByUsername(username)).thenReturn(Optional.of(testAuthorDto));

        // When
        String result = userService.getUserBio(username);

        // Then
        assertEquals("Test bio", result);
    }

    @Test
    @DisplayName("Получение биографии пользователя - null когда автор не существует")
    void getUserBio_ShouldReturnNull_WhenAuthorNotExists() {
        // Given
        String username = "testuser";
        when(authorService.getAuthorByUsername(username)).thenReturn(Optional.empty());

        // When
        String result = userService.getUserBio(username);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Получение пользователя с автором и ролями - успешно")
    void getUserWithAuthorAndRoles_ShouldReturnUserWithDetails() {
        // Given
        Long userId = 1L;
        when(userRepository.findByIdWithRolesAndAuthor(userId)).thenReturn(Optional.of(testUser));
        when(userConverter.toDto(testUser)).thenReturn(testUserDto);

        // When
        Optional<UserDto> result = userService.getUserWithAuthorAndRoles(userId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testUserDto, result.get());
        assertTrue(result.get().isAuthor());
    }
}