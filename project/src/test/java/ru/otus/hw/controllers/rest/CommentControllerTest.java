package ru.otus.hw.controllers.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.config.SecurityConfig;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.UserDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.utils.MessageProvider;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
@Import(SecurityConfig.class)
class CommentControllerTest {

    private static final Long RECIPE_ID = 1L;
    private static final Long COMMENT_ID = 1L;
    private static final Long USER_ID = 1L;
    private static final String USERNAME = "test_user";
    private static final String CONTENT = "Test comment content";
    private static final String UPDATED_CONTENT = "Updated comment content";

    private static final String AUTH_REQUIRED_MESSAGE = "Требуется аутентификация";
    private static final String COMMENT_CREATED_MESSAGE = "Комментарий создан";
    private static final String COMMENT_UPDATED_MESSAGE = "Комментарий обновлен";
    private static final String COMMENT_DELETED_MESSAGE = "Комментарий удален";
    private static final String COMMENT_CREATE_ERROR_MESSAGE = "Ошибка создания комментария";
    private static final String COMMENT_LOAD_ERROR_MESSAGE = "Ошибка загрузки комментариев";
    private static final String COMMENT_UPDATE_ERROR_MESSAGE = "Ошибка обновления комментария";
    private static final String COMMENT_DELETE_ERROR_MESSAGE = "Ошибка удаления комментария";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentService commentService;

    @MockBean
    private MessageProvider messageProvider;

    private CommentDto testCommentDto;
    private UserDto testUserDto;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        testUserDto = new UserDto(
                USER_ID,
                USERNAME,
                "test@example.com",
                true,
                Set.of("ROLE_USER"),
                LocalDateTime.now(),
                false
        );

        testCommentDto = new CommentDto(
                COMMENT_ID,
                CONTENT,
                testUserDto,
                RECIPE_ID,
                LocalDateTime.now(),
                LocalDateTime.now(),
                true,
                true
        );

        authentication = new UsernamePasswordAuthenticationToken(
                USERNAME,
                null,
                List.of()
        );

        // Mock message provider responses
        when(messageProvider.getMessage("auth.required")).thenReturn(AUTH_REQUIRED_MESSAGE);
        when(messageProvider.getMessage("comment.created")).thenReturn(COMMENT_CREATED_MESSAGE);
        when(messageProvider.getMessage("comment.updated")).thenReturn(COMMENT_UPDATED_MESSAGE);
        when(messageProvider.getMessage("comment.deleted")).thenReturn(COMMENT_DELETED_MESSAGE);
        when(messageProvider.getMessage("comment.create_error")).thenReturn(COMMENT_CREATE_ERROR_MESSAGE);
        when(messageProvider.getMessage("comment.load_error")).thenReturn(COMMENT_LOAD_ERROR_MESSAGE);
        when(messageProvider.getMessage("comment.update_error")).thenReturn(COMMENT_UPDATE_ERROR_MESSAGE);
        when(messageProvider.getMessage("comment.delete_error")).thenReturn(COMMENT_DELETE_ERROR_MESSAGE);
    }

    @Test
    @DisplayName("Создание комментария - успешный случай")
    void createComment_ShouldCreateComment_WhenValidRequest() throws Exception {
        // Arrange
        String requestBody = String.format("{\"content\": \"%s\"}", CONTENT);

        when(commentService.createCommentForRecipe(eq(CONTENT), eq(USERNAME), eq(RECIPE_ID)))
                .thenReturn(testCommentDto);

        // Act & Assert
        mockMvc.perform(post("/api/recipes/{recipeId}/comments", RECIPE_ID)
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(COMMENT_ID))
                .andExpect(jsonPath("$.data.content").value(CONTENT))
                .andExpect(jsonPath("$.data.recipeId").value(RECIPE_ID))
                .andExpect(jsonPath("$.data.user.id").value(USER_ID))
                .andExpect(jsonPath("$.data.user.username").value(USERNAME))
                .andExpect(jsonPath("$.data.canEdit").value(true))
                .andExpect(jsonPath("$.data.canDelete").value(true))
                .andExpect(jsonPath("$.message").value(COMMENT_CREATED_MESSAGE));

        verify(commentService).createCommentForRecipe(CONTENT, USERNAME, RECIPE_ID);
    }

    @Test
    @DisplayName("Создание комментария - не аутентифицированный пользователь")
    void createComment_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // Arrange
        String requestBody = String.format("{\"content\": \"%s\"}", CONTENT);

        // Act & Assert
        mockMvc.perform(post("/api/recipes/{recipeId}/comments", RECIPE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("Создание комментария - рецепт не найден")
    void createComment_ShouldReturnNotFound_WhenRecipeNotFound() throws Exception {
        // Arrange
        String requestBody = String.format("{\"content\": \"%s\"}", CONTENT);
        String errorMessage = "Рецепт не найден";

        when(commentService.createCommentForRecipe(eq(CONTENT), eq(USERNAME), eq(RECIPE_ID)))
                .thenThrow(new EntityNotFoundException(errorMessage));

        // Act & Assert
        mockMvc.perform(post("/api/recipes/{recipeId}/comments", RECIPE_ID)
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(errorMessage));
    }

    @Test
    @DisplayName("Создание комментария - неверный запрос")
    void createComment_ShouldReturnBadRequest_WhenInvalidRequest() throws Exception {
        // Arrange
        String requestBody = "{\"content\": \"\"}";

        // Act & Assert
        mockMvc.perform(post("/api/recipes/{recipeId}/comments", RECIPE_ID)
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Создание комментария - внутренняя ошибка сервера")
    void createComment_ShouldReturnInternalError_WhenServiceFails() throws Exception {
        // Arrange
        String requestBody = String.format("{\"content\": \"%s\"}", CONTENT);

        when(commentService.createCommentForRecipe(anyString(), anyString(), anyLong()))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(post("/api/recipes/{recipeId}/comments", RECIPE_ID)
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(COMMENT_CREATE_ERROR_MESSAGE));
    }

    @Test
    @DisplayName("Получение комментариев рецепта - успешный случай с аутентификацией")
    void getCommentsByRecipe_ShouldReturnComments_WhenAuthenticated() throws Exception {
        // Arrange
        List<CommentDto> comments = List.of(testCommentDto);
        when(commentService.getCommentsForRecipe(eq(RECIPE_ID), eq(USERNAME)))
                .thenReturn(comments);

        // Act & Assert
        mockMvc.perform(get("/api/recipes/{recipeId}/comments", RECIPE_ID)
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(COMMENT_ID))
                .andExpect(jsonPath("$.data[0].content").value(CONTENT))
                .andExpect(jsonPath("$.data[0].recipeId").value(RECIPE_ID))
                .andExpect(jsonPath("$.data[0].user.id").value(USER_ID));
    }

    @Test
    @DisplayName("Получение комментариев рецепта - успешный случай без аутентификации")
    void getCommentsByRecipe_ShouldReturnComments_WhenNotAuthenticated() throws Exception {
        // Arrange
        List<CommentDto> comments = List.of(testCommentDto);
        when(commentService.getCommentsForRecipe(eq(RECIPE_ID), isNull()))
                .thenReturn(comments);

        // Act & Assert
        mockMvc.perform(get("/api/recipes/{recipeId}/comments", RECIPE_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(COMMENT_ID));
    }

    @Test
    @DisplayName("Получение комментариев рецепта - пустой список")
    void getCommentsByRecipe_ShouldReturnEmptyList_WhenNoComments() throws Exception {
        // Arrange
        when(commentService.getCommentsForRecipe(eq(RECIPE_ID), anyString()))
                .thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/recipes/{recipeId}/comments", RECIPE_ID)
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("Получение комментариев рецепта - внутренняя ошибка сервера")
    void getCommentsByRecipe_ShouldReturnInternalError_WhenServiceFails() throws Exception {
        // Arrange
        when(commentService.getCommentsForRecipe(anyLong(), anyString()))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(get("/api/recipes/{recipeId}/comments", RECIPE_ID)
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(COMMENT_LOAD_ERROR_MESSAGE));
    }

    @Test
    @DisplayName("Обновление комментария - успешный случай")
    void updateComment_ShouldUpdateComment_WhenValidRequest() throws Exception {
        // Arrange
        String requestBody = String.format("{\"content\": \"%s\"}", UPDATED_CONTENT);
        CommentDto updatedCommentDto = new CommentDto(
                COMMENT_ID,
                UPDATED_CONTENT,
                testUserDto,
                RECIPE_ID,
                LocalDateTime.now(),
                LocalDateTime.now(),
                true,
                true
        );

        when(commentService.getUserIdByUsername(USERNAME)).thenReturn(USER_ID);
        when(commentService.updateComment(eq(COMMENT_ID), eq(UPDATED_CONTENT), eq(USER_ID)))
                .thenReturn(updatedCommentDto);

        // Act & Assert
        mockMvc.perform(put("/api/recipes/{recipeId}/comments/{commentId}", RECIPE_ID, COMMENT_ID)
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(COMMENT_ID))
                .andExpect(jsonPath("$.data.content").value(UPDATED_CONTENT))
                .andExpect(jsonPath("$.message").value(COMMENT_UPDATED_MESSAGE));

        verify(commentService).updateComment(COMMENT_ID, UPDATED_CONTENT, USER_ID);
    }

    @Test
    @DisplayName("Обновление комментария - не аутентифицированный пользователь")
    void updateComment_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // Arrange
        String requestBody = String.format("{\"content\": \"%s\"}", UPDATED_CONTENT);

        // Act & Assert
        mockMvc.perform(put("/api/recipes/{recipeId}/comments/{commentId}", RECIPE_ID, COMMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("Обновление комментария - доступ запрещен")
    void updateComment_ShouldReturnForbidden_WhenUserNotOwner() throws Exception {
        // Arrange
        String requestBody = String.format("{\"content\": \"%s\"}", UPDATED_CONTENT);
        String errorMessage = "Недостаточно прав для редактирования комментария";

        when(commentService.getUserIdByUsername(USERNAME)).thenReturn(USER_ID);
        when(commentService.updateComment(anyLong(), anyString(), anyLong()))
                .thenThrow(new SecurityException(errorMessage));

        // Act & Assert
        mockMvc.perform(put("/api/recipes/{recipeId}/comments/{commentId}", RECIPE_ID, COMMENT_ID)
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(errorMessage));
    }

    @Test
    @DisplayName("Обновление комментария - комментарий не найден")
    void updateComment_ShouldReturnNotFound_WhenCommentNotFound() throws Exception {
        // Arrange
        String requestBody = String.format("{\"content\": \"%s\"}", UPDATED_CONTENT);
        String errorMessage = "Комментарий не найден";

        when(commentService.getUserIdByUsername(USERNAME)).thenReturn(USER_ID);
        when(commentService.updateComment(anyLong(), anyString(), anyLong()))
                .thenThrow(new EntityNotFoundException(errorMessage));

        // Act & Assert
        mockMvc.perform(put("/api/recipes/{recipeId}/comments/{commentId}", RECIPE_ID, COMMENT_ID)
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(errorMessage));
    }

    @Test
    @DisplayName("Удаление комментария - успешный случай")
    void deleteComment_ShouldDeleteComment_WhenValidRequest() throws Exception {
        // Arrange
        when(commentService.getUserIdByUsername(USERNAME)).thenReturn(USER_ID);

        // Act & Assert
        mockMvc.perform(delete("/api/recipes/{recipeId}/comments/{commentId}", RECIPE_ID, COMMENT_ID)
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.message").value(COMMENT_DELETED_MESSAGE));

        verify(commentService).deleteComment(COMMENT_ID, USER_ID);
    }

    @Test
    @DisplayName("Удаление комментария - не аутентифицированный пользователь")
    void deleteComment_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/recipes/{recipeId}/comments/{commentId}", RECIPE_ID, COMMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("Удаление комментария - доступ запрещен")
    void deleteComment_ShouldReturnForbidden_WhenUserNotOwner() throws Exception {
        // Arrange
        String errorMessage = "Недостаточно прав для удаления комментария";

        when(commentService.getUserIdByUsername(USERNAME)).thenReturn(USER_ID);
        doThrow(new SecurityException(errorMessage))
                .when(commentService).deleteComment(anyLong(), anyLong());

        // Act & Assert
        mockMvc.perform(delete("/api/recipes/{recipeId}/comments/{commentId}", RECIPE_ID, COMMENT_ID)
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(errorMessage));
    }

    @Test
    @DisplayName("Удаление комментария - комментарий не найден")
    void deleteComment_ShouldReturnNotFound_WhenCommentNotFound() throws Exception {
        // Arrange
        String errorMessage = "Комментарий не найден";

        when(commentService.getUserIdByUsername(USERNAME)).thenReturn(USER_ID);
        doThrow(new EntityNotFoundException(errorMessage))
                .when(commentService).deleteComment(anyLong(), anyLong());

        // Act & Assert
        mockMvc.perform(delete("/api/recipes/{recipeId}/comments/{commentId}", RECIPE_ID, COMMENT_ID)
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(errorMessage));
    }

    @Test
    @DisplayName("Удаление комментария - внутренняя ошибка сервера")
    void deleteComment_ShouldReturnInternalError_WhenServiceFails() throws Exception {
        // Arrange
        when(commentService.getUserIdByUsername(USERNAME)).thenReturn(USER_ID);
        doThrow(new RuntimeException("Database error"))
                .when(commentService).deleteComment(anyLong(), anyLong());

        // Act & Assert
        mockMvc.perform(delete("/api/recipes/{recipeId}/comments/{commentId}", RECIPE_ID, COMMENT_ID)
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(COMMENT_DELETE_ERROR_MESSAGE));
    }

    @Test
    @DisplayName("Обновление комментария - внутренняя ошибка сервера")
    void updateComment_ShouldReturnInternalError_WhenServiceFails() throws Exception {
        // Arrange
        String requestBody = String.format("{\"content\": \"%s\"}", UPDATED_CONTENT);

        when(commentService.getUserIdByUsername(USERNAME)).thenReturn(USER_ID);
        when(commentService.updateComment(anyLong(), anyString(), anyLong()))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(put("/api/recipes/{recipeId}/comments/{commentId}", RECIPE_ID, COMMENT_ID)
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(COMMENT_UPDATE_ERROR_MESSAGE));
    }

}