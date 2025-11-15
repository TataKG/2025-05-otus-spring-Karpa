package ru.otus.hw.controllers.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.config.SecurityConfig;
import ru.otus.hw.dto.UserDto;
import ru.otus.hw.exceptions.EntityAlreadyExistsException;
import ru.otus.hw.services.UserService;
import ru.otus.hw.utils.MessageProvider;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    private static final Long USER_ID = 1L;
    private static final Long NON_EXISTING_USER_ID = 999L;
    private static final String USERNAME = "test_user";
    private static final String NON_EXISTING_USERNAME = "non_existing_user";
    private static final String EMAIL = "test@example.com";
    private static final String PASSWORD = "password123";
    private static final String BIO = "Test bio";

    private static final String USER_CREATED_MESSAGE = "Пользователь создан";
    private static final String USER_NOT_FOUND_MESSAGE = "Пользователь не найден";
    private static final String USER_NOT_FOUND_USERNAME_MESSAGE = "Пользователь с именем не найден";
    private static final String USER_CREATE_FAILED_MESSAGE = "Ошибка создания пользователя";
    private static final String USER_LOAD_FAILED_MESSAGE = "Ошибка загрузки пользователя";
    private static final String USER_LOAD_ALL_FAILED_MESSAGE = "Ошибка загрузки пользователей";
    private static final String USER_CHECK_EXISTS_FAILED_MESSAGE = "Ошибка проверки существования пользователя";
    private static final String USER_CHECK_EMAIL_FAILED_MESSAGE = "Ошибка проверки email";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private MessageProvider messageProvider;

    private UserDto testUserDto;

    @BeforeEach
    void setUp() {
        testUserDto = new UserDto(
                USER_ID,
                USERNAME,
                EMAIL,
                true,
                Set.of("ROLE_USER"),
                LocalDateTime.now(),
                true
        );

        // Mock message provider responses
        when(messageProvider.getMessage("user.created")).thenReturn(USER_CREATED_MESSAGE);
        when(messageProvider.getMessage("user.not_found", NON_EXISTING_USER_ID))
                .thenReturn(USER_NOT_FOUND_MESSAGE);
        when(messageProvider.getMessage("user.not_found.username", NON_EXISTING_USERNAME))
                .thenReturn(USER_NOT_FOUND_USERNAME_MESSAGE);
        when(messageProvider.getMessage("user.create.failed")).thenReturn(USER_CREATE_FAILED_MESSAGE);
        when(messageProvider.getMessage("user.load_failed")).thenReturn(USER_LOAD_FAILED_MESSAGE);
        when(messageProvider.getMessage("user.load_all_failed")).thenReturn(USER_LOAD_ALL_FAILED_MESSAGE);
        when(messageProvider.getMessage("user.check_exists_failed")).thenReturn(USER_CHECK_EXISTS_FAILED_MESSAGE);
        when(messageProvider.getMessage("user.check_email_failed")).thenReturn(USER_CHECK_EMAIL_FAILED_MESSAGE);
    }

    @Test
    @DisplayName("Создание пользователя - успешный случай")
    @WithMockUser
    void createUser_ShouldCreateUser_WhenValidRequest() throws Exception {
        // Arrange
        String requestBody = String.format(
                "{\"username\": \"%s\", \"email\": \"%s\", \"password\": \"%s\", \"bio\": \"%s\"}",
                USERNAME, EMAIL, PASSWORD, BIO
        );

        when(userService.createUser(eq(USERNAME), eq(EMAIL), eq(PASSWORD), eq(BIO)))
                .thenReturn(testUserDto);

        // Act & Assert
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(USER_ID))
                .andExpect(jsonPath("$.data.username").value(USERNAME))
                .andExpect(jsonPath("$.data.email").value(EMAIL))
                .andExpect(jsonPath("$.data.enabled").value(true))
                .andExpect(jsonPath("$.data.roles").isArray())
                .andExpect(jsonPath("$.data.isAuthor").value(true))
                .andExpect(jsonPath("$.message").value(USER_CREATED_MESSAGE));
    }

    @Test
    @DisplayName("Создание пользователя - конфликт при существующем username")
    @WithMockUser
    void createUser_ShouldReturnConflict_WhenUsernameAlreadyExists() throws Exception {
        // Arrange
        String requestBody = String.format(
                "{\"username\": \"%s\", \"email\": \"%s\", \"password\": \"%s\", \"bio\": \"%s\"}",
                USERNAME, EMAIL, PASSWORD, BIO
        );
        String errorMessage = "Пользователь с таким именем уже существует";

        when(userService.createUser(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new EntityAlreadyExistsException(errorMessage));

        // Act & Assert
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(errorMessage));
    }

    @Test
    @DisplayName("Создание пользователя - конфликт при существующем email")
    @WithMockUser
    void createUser_ShouldReturnConflict_WhenEmailAlreadyExists() throws Exception {
        // Arrange
        String requestBody = String.format(
                "{\"username\": \"%s\", \"email\": \"%s\", \"password\": \"%s\", \"bio\": \"%s\"}",
                "new_user", EMAIL, PASSWORD, BIO
        );
        String errorMessage = "Пользователь с таким email уже существует";

        when(userService.createUser(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new EntityAlreadyExistsException(errorMessage));

        // Act & Assert
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(errorMessage));
    }

    @Test
    @DisplayName("Создание пользователя - внутренняя ошибка сервера")
    @WithMockUser
    void createUser_ShouldReturnInternalError_WhenServiceFails() throws Exception {
        // Arrange
        String requestBody = String.format(
                "{\"username\": \"%s\", \"email\": \"%s\", \"password\": \"%s\", \"bio\": \"%s\"}",
                USERNAME, EMAIL, PASSWORD, BIO
        );

        when(userService.createUser(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(USER_CREATE_FAILED_MESSAGE));
    }

    @Test
    @DisplayName("Получение пользователя по ID - успешный случай")
    @WithMockUser
    void getUserById_ShouldReturnUser_WhenUserExists() throws Exception {
        // Arrange
        when(userService.getUserById(USER_ID))
                .thenReturn(java.util.Optional.of(testUserDto));

        // Act & Assert
        mockMvc.perform(get("/api/users/{id}", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(USER_ID))
                .andExpect(jsonPath("$.data.username").value(USERNAME))
                .andExpect(jsonPath("$.data.email").value(EMAIL))
                .andExpect(jsonPath("$.data.enabled").value(true))
                .andExpect(jsonPath("$.data.isAuthor").value(true));
    }

    @Test
    @DisplayName("Получение пользователя по ID - пользователь не найден")
    @WithMockUser
    void getUserById_ShouldReturnNotFound_WhenUserNotExists() throws Exception {
        // Arrange
        when(userService.getUserById(NON_EXISTING_USER_ID))
                .thenReturn(java.util.Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/users/{id}", NON_EXISTING_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(USER_NOT_FOUND_MESSAGE));
    }

    @Test
    @DisplayName("Получение пользователя по ID - внутренняя ошибка сервера")
    @WithMockUser
    void getUserById_ShouldReturnInternalError_WhenServiceFails() throws Exception {
        // Arrange
        when(userService.getUserById(anyLong()))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(get("/api/users/{id}", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(USER_LOAD_FAILED_MESSAGE));
    }

    @Test
    @DisplayName("Получение пользователя по username - успешный случай")
    @WithMockUser
    void getUserByUsername_ShouldReturnUser_WhenUserExists() throws Exception {
        // Arrange
        when(userService.getUserByUsername(USERNAME))
                .thenReturn(java.util.Optional.of(testUserDto));

        // Act & Assert
        mockMvc.perform(get("/api/users/username/{username}", USERNAME)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(USER_ID))
                .andExpect(jsonPath("$.data.username").value(USERNAME))
                .andExpect(jsonPath("$.data.email").value(EMAIL));
    }

    @Test
    @DisplayName("Получение пользователя по username - пользователь не найден")
    @WithMockUser
    void getUserByUsername_ShouldReturnNotFound_WhenUserNotExists() throws Exception {
        // Arrange
        when(userService.getUserByUsername(NON_EXISTING_USERNAME))
                .thenReturn(java.util.Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/users/username/{username}", NON_EXISTING_USERNAME)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(USER_NOT_FOUND_USERNAME_MESSAGE));
    }

    @Test
    @DisplayName("Получение пользователя по username - внутренняя ошибка сервера")
    @WithMockUser
    void getUserByUsername_ShouldReturnInternalError_WhenServiceFails() throws Exception {
        // Arrange
        when(userService.getUserByUsername(anyString()))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(get("/api/users/username/{username}", USERNAME)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(USER_LOAD_FAILED_MESSAGE));
    }

    @Test
    @DisplayName("Получение всех активных пользователей - успешный случай")
    @WithMockUser
    void getAllEnabledUsers_ShouldReturnUsersList() throws Exception {
        // Arrange
        List<UserDto> users = List.of(testUserDto);
        when(userService.getAllEnabledUsers()).thenReturn(users);

        // Act & Assert
        mockMvc.perform(get("/api/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(USER_ID))
                .andExpect(jsonPath("$.data[0].username").value(USERNAME))
                .andExpect(jsonPath("$.data[0].email").value(EMAIL));
    }

    @Test
    @DisplayName("Получение всех активных пользователей - пустой список")
    @WithMockUser
    void getAllEnabledUsers_ShouldReturnEmptyList_WhenNoUsers() throws Exception {
        // Arrange
        when(userService.getAllEnabledUsers()).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("Получение всех активных пользователей - внутренняя ошибка сервера")
    @WithMockUser
    void getAllEnabledUsers_ShouldReturnInternalError_WhenServiceFails() throws Exception {
        // Arrange
        when(userService.getAllEnabledUsers())
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(get("/api/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(USER_LOAD_ALL_FAILED_MESSAGE));
    }

    @Test
    @DisplayName("Проверка существования username - существует")
    void checkUsernameExists_ShouldReturnTrue_WhenUsernameExists() throws Exception {
        // Arrange
        when(userService.userExists(USERNAME)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/users/exists/username/{username}", USERNAME)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("Проверка существования username - не существует")
    void checkUsernameExists_ShouldReturnFalse_WhenUsernameNotExists() throws Exception {
        // Arrange
        when(userService.userExists(NON_EXISTING_USERNAME)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/users/exists/username/{username}", NON_EXISTING_USERNAME)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    @DisplayName("Проверка существования username - внутренняя ошибка сервера")
    void checkUsernameExists_ShouldReturnInternalError_WhenServiceFails() throws Exception {
        // Arrange
        when(userService.userExists(anyString()))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(get("/api/users/exists/username/{username}", USERNAME)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(USER_CHECK_EXISTS_FAILED_MESSAGE));
    }

    @Test
    @DisplayName("Проверка существования email - существует")
    void checkEmailExists_ShouldReturnTrue_WhenEmailExists() throws Exception {
        // Arrange
        when(userService.emailExists(EMAIL)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/users/exists/email/{email}", EMAIL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("Проверка существования email - не существует")
    void checkEmailExists_ShouldReturnFalse_WhenEmailNotExists() throws Exception {
        // Arrange
        String nonExistingEmail = "nonexisting@example.com";
        when(userService.emailExists(nonExistingEmail)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/users/exists/email/{email}", nonExistingEmail)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    @DisplayName("Проверка существования email - внутренняя ошибка сервера")
    void checkEmailExists_ShouldReturnInternalError_WhenServiceFails() throws Exception {
        // Arrange
        when(userService.emailExists(anyString()))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(get("/api/users/exists/email/{email}", EMAIL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(USER_CHECK_EMAIL_FAILED_MESSAGE));
    }

    @Test
    @DisplayName("Доступ к проверке существования - не аутентифицированный пользователь")
    void checkUsernameExists_WhenUnauthenticated_ShouldReturnOk() throws Exception {
        // Согласно конфигурации SecurityConfig, GET /api/users/exists/** разрешены для всех
        when(userService.userExists(USERNAME)).thenReturn(true);

        mockMvc.perform(get("/api/users/exists/username/{username}", USERNAME)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("Создание пользователя - не аутентифицированный пользователь")
    void createUser_WhenUnauthenticated_ShouldReturnUnauthorized() throws Exception {
        String requestBody = String.format(
                "{\"username\": \"%s\", \"email\": \"%s\", \"password\": \"%s\", \"bio\": \"%s\"}",
                USERNAME, EMAIL, PASSWORD, BIO
        );

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }
}