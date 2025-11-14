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
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.User;
import ru.otus.hw.repositories.UserRepository;
import ru.otus.hw.util.MessageProvider;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты для UserServiceImpl")
class UserServiceImplTest {

    private static final Long EXISTING_USER_ID = 1L;
    private static final Long NON_EXISTING_USER_ID = 999L;
    private static final Long FIRST_USER_ID = 1L;

    private static final String EXISTING_USERNAME = "chef_ivan";
    private static final String NON_EXISTING_USERNAME = "nonexistent";
    private static final String ADMIN_USERNAME = "admin";
    private static final String EXISTING_EMAIL = "ivan@example.com";
    private static final String NON_EXISTING_EMAIL = "new@example.com";

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
        testUser = new User(EXISTING_USERNAME, EXISTING_EMAIL, "encodedPassword");
        testUser.setId(EXISTING_USER_ID);
        testUser.addRole("USER");

        testUserDto = new UserDto(EXISTING_USER_ID, EXISTING_USERNAME, EXISTING_EMAIL, true,
                Set.of("USER"), LocalDateTime.now(), true);

        testAuthorDto = new AuthorDto(EXISTING_USER_ID, testUserDto, "Профессиональный шеф-повар с 15-летним опытом",
                LocalDateTime.now(), 5, List.of("USER"));
    }

    @Test
    @DisplayName("Создание пользователя - успешное создание")
    void createUser_ShouldCreateUser_WhenValidData() {
        // Arrange
        String username = "new_user";
        String email = "new@example.com";
        String password = "password123";
        String bio = "Биография нового пользователя";

        User newUser = new User(username, email, "encodedPassword");
        newUser.setId(FIRST_USER_ID);
        newUser.addRole("USER");

        UserDto newUserDto = new UserDto(FIRST_USER_ID, username, email, true,
                Set.of("USER"), LocalDateTime.now(), true);

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userRepository.count()).thenReturn(5L); // Не первый пользователь
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(userRepository.findByUsernameWithRoles(username)).thenReturn(Optional.of(newUser));
        when(userConverter.toDto(newUser)).thenReturn(newUserDto);

        // Act
        UserDto result = userService.createUser(username, email, password, bio);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(newUserDto);
        verify(userRepository).save(any(User.class));
        verify(authorService).createAuthorForUser(FIRST_USER_ID, bio);
    }

    @Test
    @DisplayName("Создание пользователя - первый пользователь становится админом")
    void createUser_ShouldMakeFirstUserAdmin_WhenFirstUser() {
        // Arrange
        String username = ADMIN_USERNAME;
        String email = "admin@example.com";
        String password = "admin123";
        String bio = "Биография администратора";

        User adminUser = new User(ADMIN_USERNAME, "admin@example.com", "encodedPassword");
        adminUser.setId(FIRST_USER_ID);
        adminUser.addRole("ADMIN");
        adminUser.addRole("USER");

        UserDto adminUserDto = new UserDto(FIRST_USER_ID, ADMIN_USERNAME, "admin@example.com", true,
                Set.of("ADMIN", "USER"), LocalDateTime.now(), true);

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userRepository.count()).thenReturn(0L); // Первый пользователь
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(adminUser);
        when(userRepository.findByUsernameWithRoles(username)).thenReturn(Optional.of(adminUser));
        when(userConverter.toDto(adminUser)).thenReturn(adminUserDto);

        // Act
        UserDto result = userService.createUser(username, email, password, bio);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.roles()).contains("ADMIN");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Создание пользователя - имя пользователя уже существует")
    void createUser_ShouldThrowException_WhenUsernameAlreadyExists() {
        // Arrange
        String username = "existing_user";
        String email = "new@example.com";
        String password = "password123";
        String bio = "Биография пользователя";
        String errorMessage = "Пользователь с таким именем уже существует";

        when(userRepository.existsByUsername(username)).thenReturn(true);
        when(messageProvider.getMessage("user.already_exists.username", username))
                .thenReturn(errorMessage);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(username, email, password, bio))
                .isInstanceOf(EntityAlreadyExistsException.class)
                .hasMessage(errorMessage);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Создание пользователя - email уже существует")
    void createUser_ShouldThrowException_WhenEmailAlreadyExists() {
        // Arrange
        String username = "new_user";
        String email = "existing@example.com";
        String password = "password123";
        String bio = "Биография пользователя";
        String errorMessage = "Пользователь с таким email уже существует";

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(true);
        when(messageProvider.getMessage("user.already_exists.email", email))
                .thenReturn(errorMessage);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(username, email, password, bio))
                .isInstanceOf(EntityAlreadyExistsException.class)
                .hasMessage(errorMessage);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Создание пользователя - ошибка при создании автора")
    void createUser_ShouldThrowException_WhenAuthorCreationFails() {
        // Arrange
        String username = "new_user";
        String email = "new@example.com";
        String password = "password123";
        String bio = "Биография нового пользователя";

        User newUser = new User(username, email, "encodedPassword");
        newUser.setId(FIRST_USER_ID);

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userRepository.count()).thenReturn(5L);
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(authorService.createAuthorForUser(FIRST_USER_ID, bio))
                .thenThrow(new RuntimeException("Ошибка создания автора"));

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(username, email, password, bio))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to create author for user");

        verify(userRepository).save(any(User.class));
        verify(authorService).createAuthorForUser(FIRST_USER_ID, bio);
    }

    @Test
    @DisplayName("Создание пользователя - пользователь не найден после сохранения")
    void createUser_ShouldThrowException_WhenUserNotFoundAfterSave() {
        // Arrange
        String username = "new_user";
        String email = "new@example.com";
        String password = "password123";
        String bio = "Биография нового пользователя";
        String errorMessage = "Пользователь не найден";

        User newUser = new User(username, email, "encodedPassword");
        newUser.setId(FIRST_USER_ID);

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userRepository.count()).thenReturn(5L);
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(userRepository.findByUsernameWithRoles(username)).thenReturn(Optional.empty());
        when(messageProvider.getMessage("user.not_found", FIRST_USER_ID))
                .thenReturn(errorMessage);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(username, email, password, bio))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage(errorMessage);

        verify(userRepository).save(any(User.class));
        verify(authorService).createAuthorForUser(FIRST_USER_ID, bio);
    }

    @Test
    @DisplayName("Получение пользователя по ID - пользователь найден")
    void getUserById_ShouldReturnUser_WhenUserExists() {
        // Arrange
        when(userRepository.findByIdWithRolesAndAuthor(EXISTING_USER_ID)).thenReturn(Optional.of(testUser));
        when(userConverter.toDto(testUser)).thenReturn(testUserDto);

        // Act
        Optional<UserDto> result = userService.getUserById(EXISTING_USER_ID);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testUserDto);
    }

    @Test
    @DisplayName("Получение пользователя по ID - пользователь не найден")
    void getUserById_ShouldReturnEmpty_WhenUserNotExists() {
        // Arrange
        when(userRepository.findByIdWithRolesAndAuthor(NON_EXISTING_USER_ID)).thenReturn(Optional.empty());

        // Act
        Optional<UserDto> result = userService.getUserById(NON_EXISTING_USER_ID);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Получение пользователя по имени пользователя - пользователь найден")
    void getUserByUsername_ShouldReturnUser_WhenUserExists() {
        // Arrange
        when(userRepository.findByUsernameWithRoles(EXISTING_USERNAME)).thenReturn(Optional.of(testUser));
        when(userConverter.toDto(testUser)).thenReturn(testUserDto);

        // Act
        Optional<UserDto> result = userService.getUserByUsername(EXISTING_USERNAME);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testUserDto);
    }

    @Test
    @DisplayName("Получение пользователя по имени пользователя - пользователь не найден")
    void getUserByUsername_ShouldReturnEmpty_WhenUserNotExists() {
        // Arrange
        when(userRepository.findByUsernameWithRoles(NON_EXISTING_USERNAME)).thenReturn(Optional.empty());

        // Act
        Optional<UserDto> result = userService.getUserByUsername(NON_EXISTING_USERNAME);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Получение всех активных пользователей - успешно")
    void getAllEnabledUsers_ShouldReturnEnabledUsers() {
        // Arrange
        List<User> users = List.of(testUser);
        when(userRepository.findAllEnabledUsers()).thenReturn(users);
        when(userConverter.toDto(testUser)).thenReturn(testUserDto);

        // Act
        List<UserDto> result = userService.getAllEnabledUsers();

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testUserDto);
    }

    @Test
    @DisplayName("Проверка существования пользователя по имени - существует")
    void userExists_ShouldReturnTrue_WhenUserExists() {
        // Arrange
        when(userRepository.existsByUsername(EXISTING_USERNAME)).thenReturn(true);

        // Act
        boolean result = userService.userExists(EXISTING_USERNAME);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Проверка существования пользователя по имени - не существует")
    void userExists_ShouldReturnFalse_WhenUserNotExists() {
        // Arrange
        when(userRepository.existsByUsername(NON_EXISTING_USERNAME)).thenReturn(false);

        // Act
        boolean result = userService.userExists(NON_EXISTING_USERNAME);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Проверка существования email - существует")
    void emailExists_ShouldReturnTrue_WhenEmailExists() {
        // Arrange
        when(userRepository.existsByEmail(EXISTING_EMAIL)).thenReturn(true);

        // Act
        boolean result = userService.emailExists(EXISTING_EMAIL);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Проверка существования email - не существует")
    void emailExists_ShouldReturnFalse_WhenEmailNotExists() {
        // Arrange
        when(userRepository.existsByEmail(NON_EXISTING_EMAIL)).thenReturn(false);

        // Act
        boolean result = userService.emailExists(NON_EXISTING_EMAIL);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Получение биографии пользователя - успешно")
    void getUserBio_ShouldReturnBio_WhenAuthorExists() {
        // Arrange
        when(authorService.getAuthorByUsername(EXISTING_USERNAME)).thenReturn(Optional.of(testAuthorDto));

        // Act
        String result = userService.getUserBio(EXISTING_USERNAME);

        // Assert
        assertThat(result).isEqualTo("Профессиональный шеф-повар с 15-летним опытом");
    }

    @Test
    @DisplayName("Получение биографии пользователя - null когда автор не существует")
    void getUserBio_ShouldReturnNull_WhenAuthorNotExists() {
        // Arrange
        when(authorService.getAuthorByUsername(NON_EXISTING_USERNAME)).thenReturn(Optional.empty());

        // Act
        String result = userService.getUserBio(NON_EXISTING_USERNAME);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Получение пользователя с автором и ролями - успешно")
    void getUserWithAuthorAndRoles_ShouldReturnUserWithDetails() {
        // Arrange
        when(userRepository.findByIdWithRolesAndAuthor(EXISTING_USER_ID)).thenReturn(Optional.of(testUser));
        when(userConverter.toDto(testUser)).thenReturn(testUserDto);

        // Act
        Optional<UserDto> result = userService.getUserWithAuthorAndRoles(EXISTING_USER_ID);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testUserDto);
        assertThat(result.get().isAuthor()).isTrue();
    }
}