package ru.otus.hw.controllers.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookFormDto;
import ru.otus.hw.security.SecurityConfig;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.GenreService;
import ru.otus.hw.services.UserDetailService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
@Import(SecurityConfig.class)
class BookControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private AuthorService authorService;

    @MockBean
    private GenreService genreService;

    @MockBean
    private UserDetailService userDetailService;

    private final Long bookId = 1L;
    private final Long forbiddenBookId = 999L; // ID книги без прав

    @Test
    @DisplayName("Получение всех книг для USER - разрешено")
    @WithMockUser(roles = "USER")
    void getAllBooks_WithUserRole_ShouldReturnOk() throws Exception {
        given(bookService.findAll()).willReturn(List.of());

        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Получение всех книг для ADMIN - разрешено")
    @WithMockUser(roles = "ADMIN")
    void getAllBooks_WithAdminRole_ShouldReturnOk() throws Exception {
        given(bookService.findAll()).willReturn(List.of());

        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Получение книги по ID для USER - разрешено")
    @WithMockUser(roles = "USER")
    void getBookById_WithUserRole_ShouldReturnOk() throws Exception {
        BookDto bookDto = new BookDto(bookId, "Test Book", null, null);
        given(bookService.findById(bookId)).willReturn(Optional.of(bookDto));

        mockMvc.perform(get("/api/v1/books/{id}", bookId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Создание книги для ADMIN - разрешено")
    @WithMockUser(roles = "ADMIN")
    void createBook_WithAdminRole_ShouldReturnOk() throws Exception {
        BookDto bookDto = new BookDto(bookId, "New Book", null, null);
        given(bookService.insert(any(BookFormDto.class))).willReturn(bookDto);

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"New Book\", \"authorId\": 1, \"genreId\": 1}")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Обновление книги для ADMIN - разрешено")
    @WithMockUser(roles = "ADMIN")
    void updateBook_WithAdminRole_ShouldReturnOk() throws Exception {
        BookDto bookDto = new BookDto(bookId, "Updated Book", null, null);
        given(bookService.update(any(BookFormDto.class))).willReturn(bookDto);

        mockMvc.perform(put("/api/v1/books/{id}", bookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": 1, \"title\": \"Updated Book\", \"authorId\": 1, \"genreId\": 1}")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Удаление книги для ADMIN - разрешено")
    @WithMockUser(roles = "ADMIN")
    void deleteBook_WithAdminRole_ShouldReturnOk() throws Exception {
        mockMvc.perform(delete("/api/v1/books/{id}", bookId)
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(bookService).deleteById(bookId);
    }

    @Test
    @DisplayName("Удаление книги для USER без прав - запрещено")
    @WithMockUser(roles = "USER")
    void deleteBook_WithUserRoleWithoutPermission_ShouldReturnForbidden() throws Exception {
        // Эмулируем ситуацию, когда у USER нет прав на удаление этой книги
        willThrow(new AccessDeniedException("Access Denied"))
                .given(bookService).deleteById(forbiddenBookId);

        mockMvc.perform(delete("/api/v1/books/{id}", forbiddenBookId)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Удаление книги для USER с правами - разрешено")
    @WithMockUser(roles = "USER")
    void deleteBook_WithUserRoleWithPermission_ShouldReturnOk() throws Exception {
        mockMvc.perform(delete("/api/v1/books/{id}", bookId)
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(bookService).deleteById(bookId);
    }

    @Test
    @DisplayName("Доступ к книгам без аутентификации - - перенаправление на логин")
    void getBooks_WithoutAuthentication_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }
}