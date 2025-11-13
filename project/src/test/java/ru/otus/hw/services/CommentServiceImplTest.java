package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.converters.CommentConverter;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.UserDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.*;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.RecipeRepository;
import ru.otus.hw.repositories.UserRepository;
import ru.otus.hw.util.MessageProvider;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты для CommentServiceImpl")
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private CommentConverter commentConverter;

    @Mock
    private MessageProvider messageProvider;

    @InjectMocks
    private CommentServiceImpl commentService;

    private User testUser;
    private Recipe testRecipe;
    private Comment testComment;
    private CommentDto testCommentDto;
    private UserDto testUserDto;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "password");
        testUser.setId(1L);

        testRecipe = new Recipe("Test Recipe", new Category("Test Category", "Desc"),
                new Author(testUser, "Bio"), "Test Description");
        testRecipe.setId(1L);
        testRecipe.setPublished(true);

        testComment = new Comment("Test comment content", testUser, testRecipe);
        testComment.setId(1L);

        testUserDto = new UserDto(1L, "testuser", "test@example.com", true,
                java.util.Set.of("USER"), LocalDateTime.now(), true);

        testCommentDto = new CommentDto(1L, "Test comment content", testUserDto,
                1L, LocalDateTime.now(), LocalDateTime.now(), true, true);
    }

    @Test
    @DisplayName("Создание комментария для рецепта - успешное создание")
    void createCommentForRecipe_ShouldCreateComment_WhenValidData() {
        // Given
        String content = "Test comment content";
        String username = "testuser";
        Long recipeId = 1L;

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(testRecipe));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);
        when(commentConverter.toDto(testComment, testUser.getId())).thenReturn(testCommentDto);

        // When
        CommentDto result = commentService.createCommentForRecipe(content, username, recipeId);

        // Then
        assertNotNull(result);
        assertEquals(testCommentDto, result);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("Создание комментария - пользователь не найден")
    void createCommentForRecipe_ShouldThrowException_WhenUserNotFound() {
        // Given
        String content = "Test comment content";
        String username = "nonexistent";
        Long recipeId = 1L;

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(messageProvider.getMessage("user.not_found")).thenReturn("User not found");

        // When & Then
        assertThrows(EntityNotFoundException.class,
                () -> commentService.createCommentForRecipe(content, username, recipeId));

        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("Создание комментария - рецепт не найден")
    void createCommentForRecipe_ShouldThrowException_WhenRecipeNotFound() {
        // Given
        String content = "Test comment content";
        String username = "testuser";
        Long recipeId = 1L;

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(recipeRepository.findById(recipeId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class,
                () -> commentService.createCommentForRecipe(content, username, recipeId));

        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("Создание комментария - рецепт не опубликован")
    void createCommentForRecipe_ShouldThrowException_WhenRecipeNotPublished() {
        // Given
        String content = "Test comment content";
        String username = "testuser";
        Long recipeId = 1L;
        testRecipe.setPublished(false);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(testRecipe));

        // When & Then
        assertThrows(IllegalStateException.class,
                () -> commentService.createCommentForRecipe(content, username, recipeId));

        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("Получение комментариев для рецепта - успешно для авторизованного пользователя")
    void getCommentsForRecipe_ShouldReturnComments_WhenRecipePublishedAndUserAuthenticated() {
        // Given
        Long recipeId = 1L;
        String username = "testuser";
        List<Comment> comments = List.of(testComment);

        when(recipeRepository.existsByIdAndPublishedTrue(recipeId)).thenReturn(true);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(commentRepository.findByRecipeIdWithUser(recipeId)).thenReturn(comments);
        when(commentConverter.toDto(testComment, testUser.getId())).thenReturn(testCommentDto);

        // When
        List<CommentDto> result = commentService.getCommentsForRecipe(recipeId, username);

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(testCommentDto, result.get(0));
    }

    @Test
    @DisplayName("Получение комментариев для рецепта - успешно для анонимного пользователя")
    void getCommentsForRecipe_ShouldReturnComments_WhenRecipePublishedAndUserAnonymous() {
        // Given
        Long recipeId = 1L;
        List<Comment> comments = List.of(testComment);

        when(recipeRepository.existsByIdAndPublishedTrue(recipeId)).thenReturn(true);
        when(commentRepository.findByRecipeIdWithUser(recipeId)).thenReturn(comments);
        when(commentConverter.toDto(testComment, null)).thenReturn(testCommentDto);

        // When
        List<CommentDto> result = commentService.getCommentsForRecipe(recipeId, null);

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(testCommentDto, result.get(0));
    }

    @Test
    @DisplayName("Получение комментариев для рецепта - пустой список, когда рецепт не опубликован")
    void getCommentsForRecipe_ShouldReturnEmptyList_WhenRecipeNotPublished() {
        // Given
        Long recipeId = 1L;
        String username = "testuser";

        when(recipeRepository.existsByIdAndPublishedTrue(recipeId)).thenReturn(false);

        // When
        List<CommentDto> result = commentService.getCommentsForRecipe(recipeId, username);

        // Then
        assertTrue(result.isEmpty());
        verify(commentRepository, never()).findByRecipeIdWithUser(anyLong());
    }

    @Test
    @DisplayName("Обновление комментария - успешно, когда пользователь является владельцем")
    void updateComment_ShouldUpdateComment_WhenUserIsOwner() {
        // Given
        Long commentId = 1L;
        String newContent = "Updated comment content";
        Long currentUserId = 1L;
        Comment updatedComment = new Comment(newContent, testUser, testRecipe);
        updatedComment.setId(1L);
        CommentDto updatedCommentDto = new CommentDto(1L, newContent, testUserDto,
                1L, LocalDateTime.now(), LocalDateTime.now(), true, true);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));
        when(commentRepository.save(testComment)).thenReturn(updatedComment);
        when(commentConverter.toDto(updatedComment, currentUserId)).thenReturn(updatedCommentDto);

        // When
        CommentDto result = commentService.updateComment(commentId, newContent, currentUserId);

        // Then
        assertNotNull(result);
        assertEquals(newContent, result.content());
        verify(commentRepository).save(testComment);
    }

    @Test
    @DisplayName("Обновление комментария - ошибка, когда пользователь не является владельцем")
    void updateComment_ShouldThrowException_WhenUserIsNotOwner() {
        // Given
        Long commentId = 1L;
        String newContent = "Updated comment content";
        Long currentUserId = 2L; // Другой пользователь

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));

        // When & Then
        assertThrows(SecurityException.class,
                () -> commentService.updateComment(commentId, newContent, currentUserId));

        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("Удаление комментария - успешно, когда пользователь является владельцем")
    void deleteComment_ShouldDeleteComment_WhenUserIsOwner() {
        // Given
        Long commentId = 1L;
        Long currentUserId = 1L;

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));

        // When
        commentService.deleteComment(commentId, currentUserId);

        // Then
        verify(commentRepository).delete(testComment);
    }

    @Test
    @DisplayName("Удаление комментария - ошибка, когда пользователь не является владельцем")
    void deleteComment_ShouldThrowException_WhenUserIsNotOwner() {
        // Given
        Long commentId = 1L;
        Long currentUserId = 2L; // Другой пользователь

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));

        // When & Then
        assertThrows(SecurityException.class,
                () -> commentService.deleteComment(commentId, currentUserId));

        verify(commentRepository, never()).delete(any(Comment.class));
    }

    @Test
    @DisplayName("Получение комментария по ID - комментарий найден")
    void getCommentById_ShouldReturnComment_WhenCommentExists() {
        // Given
        Long commentId = 1L;
        when(commentRepository.findByIdWithUserAndRecipe(commentId)).thenReturn(Optional.of(testComment));
        when(commentConverter.toDto(testComment)).thenReturn(testCommentDto);

        // When
        Optional<CommentDto> result = commentService.getCommentById(commentId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testCommentDto, result.get());
    }

    @Test
    @DisplayName("Получение комментария по ID с текущим пользователем - комментарий найден")
    void getCommentByIdWithCurrentUser_ShouldReturnComment_WhenCommentExists() {
        // Given
        Long commentId = 1L;
        Long currentUserId = 1L;
        when(commentRepository.findByIdWithUserAndRecipe(commentId)).thenReturn(Optional.of(testComment));
        when(commentConverter.toDto(testComment, currentUserId)).thenReturn(testCommentDto);

        // When
        Optional<CommentDto> result = commentService.getCommentById(commentId, currentUserId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testCommentDto, result.get());
    }

    @Test
    @DisplayName("Получение комментариев пользователя - успешно")
    void getCommentsByUser_ShouldReturnUserComments() {
        // Given
        Long userId = 1L;
        List<Comment> comments = List.of(testComment);
        when(commentRepository.findByUserId(userId)).thenReturn(comments);
        when(commentConverter.toDto(testComment)).thenReturn(testCommentDto);

        // When
        List<CommentDto> result = commentService.getCommentsByUser(userId);

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(testCommentDto, result.get(0));
    }

    @Test
    @DisplayName("Получение количества комментариев для рецепта - успешно")
    void getCommentCountForRecipe_ShouldReturnCount() {
        // Given
        Long recipeId = 1L;
        int expectedCount = 5;
        when(commentRepository.countByRecipeId(recipeId)).thenReturn(expectedCount);

        // When
        int result = commentService.getCommentCountForRecipe(recipeId);

        // Then
        assertEquals(expectedCount, result);
    }

    @Test
    @DisplayName("Получение ID пользователя по имени - успешно")
    void getUserIdByUsername_ShouldReturnUserId_WhenUserExists() {
        // Given
        String username = "testuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        // When
        Long result = commentService.getUserIdByUsername(username);

        // Then
        assertEquals(testUser.getId(), result);
    }

    @Test
    @DisplayName("Получение ID пользователя по имени - пользователь не найден")
    void getUserIdByUsername_ShouldThrowException_WhenUserNotFound() {
        // Given
        String username = "nonexistent";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class,
                () -> commentService.getUserIdByUsername(username));
    }
}