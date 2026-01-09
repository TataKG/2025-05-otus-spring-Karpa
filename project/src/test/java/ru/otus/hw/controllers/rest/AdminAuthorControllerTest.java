package ru.otus.hw.controllers.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import ru.otus.hw.dto.ApiResponse;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.UserDto;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.utils.MessageProvider;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты контроллера администратора для работы с авторами")
class AdminAuthorControllerTest {

    @Mock
    private AuthorService authorService;

    @Mock
    private MessageProvider messageProvider;

    @InjectMocks
    private AdminAuthorController adminAuthorController;

    @BeforeEach
    void setUp() {
        UserDetails adminUser = new User("admin", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(adminUser, null, adminUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("Получение списка авторов - успешное выполнение")
    void getAuthors_Success() {
        // Arrange
        AuthorDto authorDto = new AuthorDto(
                1L,
                new UserDto(
                        1L,
                        "shef_ivan",
                        "ivan@example.com",
                        true,
                        Set.of("USER"),
                        LocalDateTime.now(),
                        true),
                "ivan@example.com",
                LocalDateTime.now(),
                5,
                List.of("USER")
        );
        List<AuthorDto> authors = List.of(authorDto);
        when(authorService.getAllAuthors()).thenReturn(authors);

        // Act
        ResponseEntity<ApiResponse<List<AuthorDto>>> response = adminAuthorController.getAuthors();

        // Assert
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data()).isEqualTo(authors);
        verify(authorService).getAllAuthors();
    }

    @Test
    @DisplayName("Получение списка авторов - ошибка базы данных")
    void getAuthors_Exception() {
        // Arrange
        String errorMessage = "Ошибка базы данных";
        when(authorService.getAllAuthors()).thenThrow(new RuntimeException(errorMessage));
        when(messageProvider.getMessage("authors.load_error")).thenReturn("Ошибка загрузки авторов: ");

        // Act
        ResponseEntity<ApiResponse<List<AuthorDto>>> response = adminAuthorController.getAuthors();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().message()).contains(errorMessage);
        verify(authorService).getAllAuthors();
    }
}