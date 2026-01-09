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
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.config.SecurityConfig;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.UserDto;
import ru.otus.hw.utils.MessageProvider;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthorController.class)
@Import(SecurityConfig.class)
class AuthorControllerTest {

    private static final Long EXISTING_AUTHOR_ID = 1L;
    private static final Long NON_EXISTING_AUTHOR_ID = 999L;
    private static final Long EXISTING_USER_ID = 1L;
    private static final Long NON_EXISTING_USER_ID = 999L;

    private static final String AUTHOR_NOT_FOUND_MESSAGE = "Author not found";
    private static final String AUTHOR_NOT_FOUND_USER_MESSAGE = "Author not found for user";
    private static final String AUTHOR_LOAD_ERROR_MESSAGE = "Error loading author";
    private static final String AUTHORS_LOAD_ERROR_MESSAGE = "Error loading authors";

    private static final String USERNAME = "testuser";
    private static final String EMAIL = "test@example.com";
    private static final String BIO = "Test bio";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthorService authorService;

    @MockBean
    private MessageProvider messageProvider;

    private AuthorDto testAuthorDto;
    private UserDto testUserDto;

    @BeforeEach
    void setUp() {
        testUserDto = new UserDto(
                EXISTING_USER_ID,
                USERNAME,
                EMAIL,
                true,
                Set.of("ROLE_USER"),
                LocalDateTime.now(),
                true
        );

        testAuthorDto = new AuthorDto(
                EXISTING_AUTHOR_ID,
                testUserDto,
                BIO,
                LocalDateTime.now(),
                5,
                List.of("ROLE_USER")
        );

        // Mock message provider responses
        when(messageProvider.getMessage("author.not_found", NON_EXISTING_AUTHOR_ID))
                .thenReturn(AUTHOR_NOT_FOUND_MESSAGE);
        when(messageProvider.getMessage("author.not_found.user", NON_EXISTING_USER_ID))
                .thenReturn(AUTHOR_NOT_FOUND_USER_MESSAGE);
        when(messageProvider.getMessage("author.load_error"))
                .thenReturn(AUTHOR_LOAD_ERROR_MESSAGE);
        when(messageProvider.getMessage("authors.load_error"))
                .thenReturn(AUTHORS_LOAD_ERROR_MESSAGE);
    }

    @Test
    @DisplayName("Получение автора по ID - успешный случай")
    @WithMockUser // Добавляем аутентифицированного пользователя
    void getAuthorById_WhenAuthorExists_ShouldReturnAuthor() throws Exception {
        when(authorService.getAuthorById(EXISTING_AUTHOR_ID))
                .thenReturn(java.util.Optional.of(testAuthorDto));

        mockMvc.perform(get("/api/authors/{id}", EXISTING_AUTHOR_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(EXISTING_AUTHOR_ID))
                .andExpect(jsonPath("$.data.user.id").value(EXISTING_USER_ID))
                .andExpect(jsonPath("$.data.user.username").value(USERNAME))
                .andExpect(jsonPath("$.data.bio").value(BIO))
                .andExpect(jsonPath("$.data.recipeCount").value(5));
    }

    @Test
    @DisplayName("Получение автора по ID - автор не найден")
    @WithMockUser
    void getAuthorById_WhenAuthorNotExists_ShouldReturnNotFound() throws Exception {
        when(authorService.getAuthorById(NON_EXISTING_AUTHOR_ID))
                .thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/api/authors/{id}", NON_EXISTING_AUTHOR_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(AUTHOR_NOT_FOUND_MESSAGE));
    }

    @Test
    @DisplayName("Получение автора по ID пользователя - успешный случай")
    @WithMockUser
    void getAuthorByUserId_WhenAuthorExists_ShouldReturnAuthor() throws Exception {
        when(authorService.getAuthorByUserId(EXISTING_USER_ID))
                .thenReturn(java.util.Optional.of(testAuthorDto));

        mockMvc.perform(get("/api/authors/user/{userId}", EXISTING_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(EXISTING_AUTHOR_ID))
                .andExpect(jsonPath("$.data.user.id").value(EXISTING_USER_ID))
                .andExpect(jsonPath("$.data.user.username").value(USERNAME))
                .andExpect(jsonPath("$.data.bio").value(BIO));
    }

    @Test
    @DisplayName("Получение автора по ID пользователя - автор не найден")
    @WithMockUser
    void getAuthorByUserId_WhenAuthorNotExists_ShouldReturnNotFound() throws Exception {
        when(authorService.getAuthorByUserId(NON_EXISTING_USER_ID))
                .thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/api/authors/user/{userId}", NON_EXISTING_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(AUTHOR_NOT_FOUND_USER_MESSAGE));
    }

    @Test
    @DisplayName("Получение всех авторов - успешный случай")
    @WithMockUser
    void getAllAuthors_WhenAuthorsExist_ShouldReturnAuthorsList() throws Exception {
        List<AuthorDto> authors = List.of(testAuthorDto);
        when(authorService.getAllAuthors()).thenReturn(authors);

        mockMvc.perform(get("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(EXISTING_AUTHOR_ID))
                .andExpect(jsonPath("$.data[0].user.id").value(EXISTING_USER_ID))
                .andExpect(jsonPath("$.data[0].user.username").value(USERNAME))
                .andExpect(jsonPath("$.data[0].bio").value(BIO))
                .andExpect(jsonPath("$.data[0].recipeCount").value(5));
    }

    @Test
    @DisplayName("Получение всех авторов - пустой список")
    @WithMockUser
    void getAllAuthors_WhenNoAuthors_ShouldReturnEmptyList() throws Exception {
        when(authorService.getAllAuthors()).thenReturn(List.of());

        mockMvc.perform(get("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("Получение автора по ID - внутренняя ошибка сервера")
    @WithMockUser
    void getAuthorById_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        when(authorService.getAuthorById(anyLong()))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/authors/{id}", EXISTING_AUTHOR_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(AUTHOR_LOAD_ERROR_MESSAGE + "Database error"));
    }

    @Test
    @DisplayName("Получение автора по ID пользователя - внутренняя ошибка сервера")
    @WithMockUser
    void getAuthorByUserId_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        when(authorService.getAuthorByUserId(anyLong()))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/authors/user/{userId}", EXISTING_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(AUTHOR_LOAD_ERROR_MESSAGE + "Database error"));
    }

    @Test
    @DisplayName("Получение всех авторов - внутренняя ошибка сервера")
    @WithMockUser
    void getAllAuthors_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        when(authorService.getAllAuthors())
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(AUTHORS_LOAD_ERROR_MESSAGE + "Database error"));
    }

    @Test
    @DisplayName("Получение автора по ID - EntityNotFoundException обрабатывается корректно")
    @WithMockUser
    void getAuthorById_WhenEntityNotFoundExceptionThrown_ShouldReturnNotFound() throws Exception {
        when(authorService.getAuthorById(EXISTING_AUTHOR_ID))
                .thenThrow(new EntityNotFoundException(AUTHOR_NOT_FOUND_MESSAGE));

        mockMvc.perform(get("/api/authors/{id}", EXISTING_AUTHOR_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(AUTHOR_NOT_FOUND_MESSAGE));
    }

    @Test
    @DisplayName("Получение всех авторов - неаутентифицированный пользователь")
    void getAllAuthors_WhenUnauthenticated_ShouldReturnUnauthorized() throws Exception {
        // Этот тест проверяет, что без аутентификации возвращается 401
        mockMvc.perform(get("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}