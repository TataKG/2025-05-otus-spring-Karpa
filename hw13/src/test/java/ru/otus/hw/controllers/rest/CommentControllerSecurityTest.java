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
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.security.SecurityConfig;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.UserDetailService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
@Import(SecurityConfig.class)
class CommentControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentService commentService;

    @MockBean
    private UserDetailService userDetailService;

    private final Long bookId = 1L;
    private final Long commentId = 1L;
    private final Long forbiddenCommentId = 999L; // ID комментария без прав

    @Test
    @DisplayName("Получение комментариев для USER - разрешено")
    @WithMockUser(roles = "USER")
    void getCommentsByBookId_WithUserRole_ShouldReturnOk() throws Exception {
        given(commentService.findByBookId(bookId)).willReturn(List.of());

        mockMvc.perform(get("/api/v1/books/{bookId}/comments", bookId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Получение комментариев для ADMIN - разрешено")
    @WithMockUser(roles = "ADMIN")
    void getCommentsByBookId_WithAdminRole_ShouldReturnOk() throws Exception {
        given(commentService.findByBookId(bookId)).willReturn(List.of());

        mockMvc.perform(get("/api/v1/books/{bookId}/comments", bookId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Создание комментария для USER - разрешено")
    @WithMockUser(roles = "USER")
    void createComment_WithUserRole_ShouldReturnOk() throws Exception {
        CommentDto commentDto = new CommentDto(0L, "Test comment", bookId);
        given(commentService.insert(any(CommentDto.class))).willReturn(commentDto);

        mockMvc.perform(post("/api/v1/books/{bookId}/comments", bookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\": \"Test comment\", \"bookId\": 1}")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Создание комментария для ADMIN - разрешено")
    @WithMockUser(roles = "ADMIN")
    void createComment_WithAdminRole_ShouldReturnOk() throws Exception {
        CommentDto commentDto = new CommentDto(0L, "Test comment", bookId);
        given(commentService.insert(any(CommentDto.class))).willReturn(commentDto);

        mockMvc.perform(post("/api/v1/books/{bookId}/comments", bookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\": \"Test comment\", \"bookId\": 1}")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Удаление комментария для ADMIN - разрешено")
    @WithMockUser(roles = "ADMIN")
    void deleteComment_WithAdminRole_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/books/{bookId}/comments/{commentId}", bookId, commentId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(commentService).deleteById(commentId);
    }

    @Test
    @DisplayName("Удаление комментария для USER без прав - запрещено")
    @WithMockUser(roles = "USER")
    void deleteComment_WithUserRoleWithoutPermission_ShouldReturnForbidden() throws Exception {
        // Эмулируем ситуацию, когда у USER нет прав на удаление этого комментария
        willThrow(new AccessDeniedException("Access Denied"))
                .given(commentService).deleteById(forbiddenCommentId);

        mockMvc.perform(delete("/api/v1/books/{bookId}/comments/{commentId}", bookId, forbiddenCommentId))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Удаление комментария для USER с правами - разрешено")
    @WithMockUser(roles = "USER")
    void deleteComment_WithUserRoleWithPermission_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/books/{bookId}/comments/{commentId}", bookId, commentId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(commentService).deleteById(commentId);
    }

    @Test
    @DisplayName("Доступ к комментариям без аутентификации - перенаправление на логин")
    void getComments_WithoutAuthentication_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/api/v1/books/{bookId}/comments", bookId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }
}