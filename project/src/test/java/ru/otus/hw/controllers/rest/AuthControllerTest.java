package ru.otus.hw.controllers.rest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import ru.otus.hw.dto.ApiResponse;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.UserDto;
import ru.otus.hw.exceptions.EntityAlreadyExistsException;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.UserService;
import ru.otus.hw.util.MessageProvider;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты для AuthController")
class AuthControllerTest {

    private static final Long USER_ID = 1L;
    private static final String USERNAME = "test_user";
    private static final String EMAIL = "test@example.com";
    private static final String PASSWORD = "password123";
    private static final String BIO = "Test biography";

    @Mock
    private UserService userService;

    @Mock
    private AuthorService authorService;

    @Mock
    private MessageProvider messageProvider;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthController authController;

    private UserDto testUserDto;
    private AuthorDto testAuthorDto;
    private AuthController.RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        testUserDto = createUserDto(Set.of("USER"));
        testAuthorDto = createAuthorDto(testUserDto, List.of("USER"));
        registerRequest = createRegisterRequest(BIO);
    }

    @Test
    @DisplayName("Получение текущего пользователя - аутентифицированный пользователь с автором")
    void getCurrentUser_ShouldReturnUserWithAuthor_WhenAuthenticatedWithAuthor() {
        // Arrange
        Collection<? extends GrantedAuthority> authorities = createAuthorities("ROLE_USER", "ROLE_ADMIN");
        setupAuthentication(authorities);
        when(authorService.getAuthorByUsername(USERNAME)).thenReturn(Optional.of(testAuthorDto));

        // Act
        ResponseEntity<ApiResponse<AuthController.AuthUserResponse>> response =
                authController.getCurrentUser(authentication);

        // Assert
        assertSuccessResponse(response, HttpStatus.OK);
        AuthController.AuthUserResponse authUser = getResponseData(response);
        assertAuthenticatedUser(authUser, List.of("USER", "ADMIN"));
    }

    @Test
    @DisplayName("Получение текущего пользователя - не аутентифицированный")
    void getCurrentUser_ShouldReturnUnauthenticated_WhenNotAuthenticated() {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(false);

        // Act
        ResponseEntity<ApiResponse<AuthController.AuthUserResponse>> response =
                authController.getCurrentUser(authentication);

        // Assert
        assertSuccessResponse(response, HttpStatus.OK);
        assertUnauthenticatedUser(getResponseData(response));
    }

    @Test
    @DisplayName("Получение текущего пользователя - null authentication")
    void getCurrentUser_ShouldReturnUnauthenticated_WhenAuthenticationIsNull() {
        // Act
        ResponseEntity<ApiResponse<AuthController.AuthUserResponse>> response =
                authController.getCurrentUser(null);

        // Assert
        assertSuccessResponse(response, HttpStatus.OK);
        assertUnauthenticatedUser(getResponseData(response));
    }

    @Test
    @DisplayName("Получение текущего пользователя - исключение при получении автора")
    void getCurrentUser_ShouldHandleException_WhenAuthorServiceThrowsException() {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(USERNAME);
        when(authorService.getAuthorByUsername(USERNAME)).thenThrow(new RuntimeException("Service error"));

        // Act
        ResponseEntity<ApiResponse<AuthController.AuthUserResponse>> response =
                authController.getCurrentUser(authentication);

        // Assert
        assertSuccessResponse(response, HttpStatus.OK);
        AuthController.AuthUserResponse authUser = getResponseData(response);

        // Проверяем только то, что действительно важно в этом сценарии
        assertThat(authUser.authenticated()).isTrue();
        assertThat(authUser.name()).isEqualTo(USERNAME);
        // Роли могут быть null или пустыми в этом случае
        assertThat(authUser.bio()).isNull();
        assertThat(authUser.isAdmin()).isFalse();
    }

    @Test
    @DisplayName("Регистрация - успешная регистрация")
    void register_ShouldCreateUser_WhenValidRequest() {
        // Arrange
        String successMessage = "Пользователь успешно создан";
        when(userService.createUser(any(), any(), any(), any())).thenReturn(testUserDto);
        when(messageProvider.getMessage("user.created")).thenReturn(successMessage);

        // Act
        ResponseEntity<ApiResponse<UserDto>> response = authController.register(registerRequest);

        // Assert
        assertSuccessResponse(response, HttpStatus.CREATED);
        assertThat(getResponseData(response)).isEqualTo(testUserDto);
        assertThat(getResponseMessage(response)).isEqualTo(successMessage);

        verify(userService).createUser(
                registerRequest.username(),
                registerRequest.email(),
                registerRequest.password(),
                registerRequest.bio()
        );
    }

    @Test
    @DisplayName("Регистрация - успешная регистрация без сообщения")
    void register_ShouldCreateUserWithoutMessage_WhenValidRequest() {
        // Arrange
        when(userService.createUser(any(), any(), any(), any())).thenReturn(testUserDto);

        // Act
        ResponseEntity<ApiResponse<UserDto>> response = authController.register(registerRequest);

        // Assert
        assertSuccessResponse(response, HttpStatus.CREATED);
        assertThat(getResponseData(response)).isEqualTo(testUserDto);
        assertThat(getResponseMessage(response)).isNull();
    }

    @Test
    @DisplayName("Регистрация - конфликт при существующем пользователе")
    void register_ShouldReturnConflict_WhenUserAlreadyExists() {
        // Arrange
        String errorMessage = "Пользователь уже существует";
        when(userService.createUser(any(), any(), any(), any()))
                .thenThrow(new EntityAlreadyExistsException(errorMessage));

        // Act
        ResponseEntity<ApiResponse<UserDto>> response = authController.register(registerRequest);

        // Assert
        assertErrorResponse(response, HttpStatus.CONFLICT, errorMessage);
    }

    @Test
    @DisplayName("Регистрация - внутренняя ошибка сервера")
    void register_ShouldReturnInternalError_WhenServiceFails() {
        // Arrange
        String errorMessage = "Ошибка регистрации";
        when(userService.createUser(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Database error"));
        when(messageProvider.getMessage("user.register_error")).thenReturn(errorMessage);

        // Act
        ResponseEntity<ApiResponse<UserDto>> response = authController.register(registerRequest);

        // Assert
        assertErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
    }

    @Test
    @DisplayName("Логин - успешный ответ")
    void login_ShouldReturnSuccessMessage() {
        // Arrange
        String successMessage = "Успешный вход в систему";
        when(messageProvider.getMessage("auth.login_success")).thenReturn(successMessage);

        // Act
        ResponseEntity<ApiResponse<String>> response = authController.login(
                new AuthController.LoginRequest(USERNAME, PASSWORD));

        // Assert
        assertSuccessResponse(response, HttpStatus.OK);
        assertThat(getResponseData(response)).isEqualTo(successMessage);
        assertThat(getResponseMessage(response)).isNull();
    }

    @Test
    @DisplayName("Получение текущего пользователя - пользователь с ролью USER (не админ)")
    void getCurrentUser_ShouldReturnNonAdmin_WhenUserHasUserRoleOnly() {
        // Arrange
        Collection<? extends GrantedAuthority> authorities = createAuthorities("ROLE_USER");
        setupAuthentication(authorities);
        when(authorService.getAuthorByUsername(USERNAME)).thenReturn(Optional.of(testAuthorDto));

        // Act
        ResponseEntity<ApiResponse<AuthController.AuthUserResponse>> response =
                authController.getCurrentUser(authentication);

        // Assert
        AuthController.AuthUserResponse authUser = getResponseData(response);
        assertThat(authUser.isAdmin()).isFalse();
        assertThat(authUser.roles()).containsExactly("USER");
    }

    @Test
    @DisplayName("Регистрация - обработка null bio")
    void register_ShouldHandleNullBio() {
        // Arrange
        AuthController.RegisterRequest requestWithNullBio = createRegisterRequest(null);
        when(userService.createUser(any(), any(), any(), any())).thenReturn(testUserDto);

        // Act
        ResponseEntity<ApiResponse<UserDto>> response = authController.register(requestWithNullBio);

        // Assert
        assertSuccessResponse(response, HttpStatus.CREATED);

        verify(userService).createUser(
                requestWithNullBio.username(),
                requestWithNullBio.email(),
                requestWithNullBio.password(),
                null
        );
    }

    private UserDto createUserDto(Set<String> roles) {
        return new UserDto(AuthControllerTest.USER_ID, AuthControllerTest.USERNAME, AuthControllerTest.EMAIL, true, roles, LocalDateTime.now(), true);
    }

    private AuthorDto createAuthorDto(UserDto user, List<String> roles) {
        return new AuthorDto(AuthControllerTest.USER_ID, user, AuthControllerTest.BIO, LocalDateTime.now(), 0, roles);
    }

    private AuthController.RegisterRequest createRegisterRequest(String bio) {
        return new AuthController.RegisterRequest("new_user", "new@example.com", AuthControllerTest.PASSWORD, bio);
    }

    private Collection<? extends GrantedAuthority> createAuthorities(String... roles) {
        return Stream.of(roles)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    private void setupAuthentication(Collection<? extends GrantedAuthority> authorities) {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(AuthControllerTest.USERNAME);
        when(authentication.getAuthorities()).thenAnswer(invocation -> authorities);
    }

    private <T> void assertSuccessResponse(ResponseEntity<ApiResponse<T>> response, HttpStatus expectedStatus) {
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    private <T> void assertErrorResponse(ResponseEntity<ApiResponse<T>> response, HttpStatus expectedStatus, String expectedMessage) {
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().message()).isEqualTo(expectedMessage);
        assertThat(response.getBody().data()).isNull();
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    private <T> T getResponseData(ResponseEntity<ApiResponse<T>> response) {
        Assertions.assertNotNull(response.getBody());
        return response.getBody().data();
    }

    private <T> String getResponseMessage(ResponseEntity<ApiResponse<T>> response) {
        Assertions.assertNotNull(response.getBody());
        return response.getBody().message();
    }

    private void assertAuthenticatedUser(AuthController.AuthUserResponse authUser,
                                         List<String> expectedRoles) {
        assertThat(authUser.authenticated()).isTrue();
        assertThat(authUser.name()).isEqualTo(AuthControllerTest.USERNAME);
        assertThat(authUser.roles()).isEqualTo(expectedRoles);
        assertThat(authUser.bio()).isEqualTo(AuthControllerTest.BIO);
        assertThat(authUser.isAdmin()).isEqualTo(true);
    }

    private void assertUnauthenticatedUser(AuthController.AuthUserResponse authUser) {
        assertThat(authUser.authenticated()).isFalse();
        assertThat(authUser.name()).isNull();
        assertThat(authUser.roles()).isNull();
        assertThat(authUser.bio()).isNull();
        assertThat(authUser.isAdmin()).isFalse();
    }
}