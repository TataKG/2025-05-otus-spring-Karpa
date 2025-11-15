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
import ru.otus.hw.utils.MessageProvider;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты для CommentServiceImpl")
class CommentServiceImplTest {

    private static final Long EXISTING_USER_ID = 1L;
    private static final Long ANOTHER_USER_ID = 2L;
    private static final Long EXISTING_RECIPE_ID = 1L;
    private static final Long NON_EXISTING_RECIPE_ID = 999L;
    private static final Long EXISTING_COMMENT_ID = 1L;
    private static final Long NON_EXISTING_COMMENT_ID = 999L;

    private static final String EXISTING_USERNAME = "test_user";
    private static final String NON_EXISTING_USERNAME = "nonexistent";

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
        testUser = new User(EXISTING_USERNAME, "test@example.com", "password");
        testUser.setId(EXISTING_USER_ID);

        testRecipe = new Recipe("Тестовый рецепт", new Category("Тестовая категория", "Описание"), new Author(testUser, "Биография"), "Тестовое описание");
        testRecipe.setId(EXISTING_RECIPE_ID);
        testRecipe.setPublished(true);

        testComment = new Comment("Тестовый комментарий", testUser, testRecipe);
        testComment.setId(EXISTING_COMMENT_ID);

        testUserDto = new UserDto(EXISTING_USER_ID, EXISTING_USERNAME, "test@example.com", true, Set.of("USER"), LocalDateTime.now(), true);

        testCommentDto = new CommentDto(EXISTING_COMMENT_ID, "Тестовый комментарий", testUserDto, EXISTING_RECIPE_ID, LocalDateTime.now(), LocalDateTime.now(), true, true);
    }

    @Test
    @DisplayName("Создание комментария для рецепта - успешное создание")
    void createCommentForRecipe_ShouldCreateComment_WhenValidData() {
        // Arrange
        String content = "Тестовый комментарий";
        String username = EXISTING_USERNAME;
        Long recipeId = EXISTING_RECIPE_ID;

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(testRecipe));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);
        when(commentConverter.toDto(testComment, testUser.getId())).thenReturn(testCommentDto);

        // Act
        CommentDto result = commentService.createCommentForRecipe(content, username, recipeId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(testCommentDto);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("Создание комментария - пользователь не найден")
    void createCommentForRecipe_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        String content = "Тестовый комментарий";
        String username = NON_EXISTING_USERNAME;

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(messageProvider.getMessage("user.not_found")).thenReturn("Пользователь не найден");

        // Act & Assert
        assertThatThrownBy(() -> commentService.createCommentForRecipe(content, username, EXISTING_RECIPE_ID)).isInstanceOf(EntityNotFoundException.class).hasMessage("Пользователь не найден");

        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("Создание комментария - рецепт не найден")
    void createCommentForRecipe_ShouldThrowException_WhenRecipeNotFound() {
        // Arrange
        String content = "Тестовый комментарий";
        String username = EXISTING_USERNAME;
        Long recipeId = NON_EXISTING_RECIPE_ID;

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(recipeRepository.findById(recipeId)).thenReturn(Optional.empty());
        when(messageProvider.getMessage("recipe.not_found", recipeId)).thenReturn("Рецепт не найден: " + recipeId);

        // Act & Assert
        assertThatThrownBy(() -> commentService.createCommentForRecipe(content, username, recipeId)).isInstanceOf(EntityNotFoundException.class).hasMessage("Рецепт не найден: " + recipeId);

        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("Создание комментария - рецепт не опубликован")
    void createCommentForRecipe_ShouldThrowException_WhenRecipeNotPublished() {
        // Arrange
        String content = "Тестовый комментарий";
        String username = EXISTING_USERNAME;
        Long recipeId = EXISTING_RECIPE_ID;
        testRecipe.setPublished(false);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(testRecipe));
        when(messageProvider.getMessage("comment.unpublished_recipe"))
                .thenReturn("Нельзя комментировать неопубликованные рецепты");

        // Act & Assert
        assertThatThrownBy(() -> commentService.createCommentForRecipe(content, username, recipeId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Нельзя комментировать неопубликованные рецепты");

        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("Получение комментариев для рецепта - успешно для авторизованного пользователя")
    void getCommentsForRecipe_ShouldReturnComments_WhenRecipePublishedAndUserAuthenticated() {
        // Arrange
        Long recipeId = EXISTING_RECIPE_ID;
        String username = EXISTING_USERNAME;
        List<Comment> comments = List.of(testComment);

        when(recipeRepository.existsByIdAndPublishedTrue(recipeId)).thenReturn(true);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(commentRepository.findByRecipeIdWithUser(recipeId)).thenReturn(comments);
        when(commentConverter.toDto(testComment, testUser.getId())).thenReturn(testCommentDto);

        // Act
        List<CommentDto> result = commentService.getCommentsForRecipe(recipeId, username);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testCommentDto);
    }

    @Test
    @DisplayName("Получение комментариев для рецепта - успешно для анонимного пользователя")
    void getCommentsForRecipe_ShouldReturnComments_WhenRecipePublishedAndUserAnonymous() {
        // Arrange
        Long recipeId = EXISTING_RECIPE_ID;
        List<Comment> comments = List.of(testComment);

        when(recipeRepository.existsByIdAndPublishedTrue(recipeId)).thenReturn(true);
        when(commentRepository.findByRecipeIdWithUser(recipeId)).thenReturn(comments);
        when(commentConverter.toDto(testComment, null)).thenReturn(testCommentDto);

        // Act
        List<CommentDto> result = commentService.getCommentsForRecipe(recipeId, null);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testCommentDto);
    }

    @Test
    @DisplayName("Получение комментариев для рецепта - пустой список, когда рецепт не опубликован")
    void getCommentsForRecipe_ShouldReturnEmptyList_WhenRecipeNotPublished() {
        // Arrange
        Long recipeId = EXISTING_RECIPE_ID;

        when(recipeRepository.existsByIdAndPublishedTrue(recipeId)).thenReturn(false);

        // Act
        List<CommentDto> result = commentService.getCommentsForRecipe(recipeId, EXISTING_USERNAME);

        // Assert
        assertThat(result).isEmpty();
        verify(commentRepository, never()).findByRecipeIdWithUser(anyLong());
    }

    @Test
    @DisplayName("Обновление комментария - успешно, когда пользователь является владельцем")
    void updateComment_ShouldUpdateComment_WhenUserIsOwner() {
        // Arrange
        Long commentId = EXISTING_COMMENT_ID;
        String newContent = "Обновленный комментарий";
        Long currentUserId = EXISTING_USER_ID;
        Comment updatedComment = new Comment(newContent, testUser, testRecipe);
        updatedComment.setId(EXISTING_COMMENT_ID);
        CommentDto updatedCommentDto = new CommentDto(EXISTING_COMMENT_ID, newContent, testUserDto, EXISTING_RECIPE_ID, LocalDateTime.now(), LocalDateTime.now(), true, true);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));
        when(commentRepository.save(testComment)).thenReturn(updatedComment);
        when(commentConverter.toDto(updatedComment, currentUserId)).thenReturn(updatedCommentDto);

        // Act
        CommentDto result = commentService.updateComment(commentId, newContent, currentUserId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo(newContent);
        verify(commentRepository).save(testComment);
    }

    @Test
    @DisplayName("Обновление комментария - ошибка, когда пользователь не является владельцем")
    void updateComment_ShouldThrowException_WhenUserIsNotOwner() {
        // Arrange
        Long commentId = EXISTING_COMMENT_ID;
        String newContent = "Обновленный комментарий";
        // Другой пользователь

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));
        when(messageProvider.getMessage("comment.edit_denied"))
                .thenReturn("Вы можете редактировать только свои комментарии");

        // Act & Assert
        assertThatThrownBy(() -> commentService.updateComment(commentId, newContent, ANOTHER_USER_ID))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Вы можете редактировать только свои комментарии");

        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("Обновление комментария - комментарий не найден")
    void updateComment_ShouldThrowException_WhenCommentNotFound() {
        // Arrange
        Long commentId = NON_EXISTING_COMMENT_ID;
        String newContent = "Обновленный комментарий";

        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());
        when(messageProvider.getMessage("comment.not_found", commentId)).thenReturn("Комментарий не найден: " + commentId);

        // Act & Assert
        assertThatThrownBy(() -> commentService.updateComment(commentId, newContent, EXISTING_USER_ID)).isInstanceOf(EntityNotFoundException.class).hasMessage("Комментарий не найден: " + commentId);

        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("Удаление комментария - успешно, когда пользователь является владельцем")
    void deleteComment_ShouldDeleteComment_WhenUserIsOwner() {
        // Arrange
        Long commentId = EXISTING_COMMENT_ID;

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));

        // Act
        commentService.deleteComment(commentId, EXISTING_USER_ID);

        // Assert
        verify(commentRepository).delete(testComment);
    }

    @Test
    @DisplayName("Удаление комментария - ошибка, когда пользователь не является владельцем")
    void deleteComment_ShouldThrowException_WhenUserIsNotOwner() {
        // Arrange
        Long commentId = EXISTING_COMMENT_ID;

        // Другой пользователь
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));
        when(messageProvider.getMessage("comment.delete_denied"))
                .thenReturn("Вы можете удалять только свои комментарии");

        // Act & Assert
        assertThatThrownBy(() -> commentService.deleteComment(commentId, ANOTHER_USER_ID))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Вы можете удалять только свои комментарии");

        verify(commentRepository, never()).delete(any(Comment.class));
    }

    @Test
    @DisplayName("Удаление комментария - комментарий не найден")
    void deleteComment_ShouldThrowException_WhenCommentNotFound() {
        // Arrange
        Long commentId = NON_EXISTING_COMMENT_ID;

        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());
        when(messageProvider.getMessage("comment.not_found", commentId)).thenReturn("Комментарий не найден: " + commentId);

        // Act & Assert
        assertThatThrownBy(() -> commentService.deleteComment(commentId, EXISTING_USER_ID)).isInstanceOf(EntityNotFoundException.class).hasMessage("Комментарий не найден: " + commentId);

        verify(commentRepository, never()).delete(any(Comment.class));
    }

    @Test
    @DisplayName("Получение комментария по ID - комментарий найден")
    void getCommentById_ShouldReturnComment_WhenCommentExists() {
        // Arrange
        Long commentId = EXISTING_COMMENT_ID;
        when(commentRepository.findByIdWithUserAndRecipe(commentId)).thenReturn(Optional.of(testComment));
        when(commentConverter.toDto(testComment)).thenReturn(testCommentDto);

        // Act
        Optional<CommentDto> result = commentService.getCommentById(commentId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testCommentDto);
    }

    @Test
    @DisplayName("Получение комментария по ID - комментарий не найден")
    void getCommentById_ShouldReturnEmpty_WhenCommentNotExists() {
        // Arrange
        Long commentId = NON_EXISTING_COMMENT_ID;
        when(commentRepository.findByIdWithUserAndRecipe(commentId)).thenReturn(Optional.empty());

        // Act
        Optional<CommentDto> result = commentService.getCommentById(commentId);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Получение комментария по ID с текущим пользователем - комментарий найден")
    void getCommentByIdWithCurrentUser_ShouldReturnComment_WhenCommentExists() {
        // Arrange
        Long commentId = EXISTING_COMMENT_ID;
        Long currentUserId = EXISTING_USER_ID;
        when(commentRepository.findByIdWithUserAndRecipe(commentId)).thenReturn(Optional.of(testComment));
        when(commentConverter.toDto(testComment, currentUserId)).thenReturn(testCommentDto);

        // Act
        Optional<CommentDto> result = commentService.getCommentById(commentId, currentUserId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testCommentDto);
    }

    @Test
    @DisplayName("Получение комментариев пользователя - успешно")
    void getCommentsByUser_ShouldReturnUserComments() {
        // Arrange
        Long userId = EXISTING_USER_ID;
        List<Comment> comments = List.of(testComment);
        when(commentRepository.findByUserId(userId)).thenReturn(comments);
        when(commentConverter.toDto(testComment)).thenReturn(testCommentDto);

        // Act
        List<CommentDto> result = commentService.getCommentsByUser(userId);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testCommentDto);
    }

    @Test
    @DisplayName("Получение количества комментариев для рецепта - успешно")
    void getCommentCountForRecipe_ShouldReturnCount() {
        // Arrange
        Long recipeId = EXISTING_RECIPE_ID;
        int expectedCount = 5;
        when(commentRepository.countByRecipeId(recipeId)).thenReturn(expectedCount);

        // Act
        int result = commentService.getCommentCountForRecipe(recipeId);

        // Assert
        assertThat(result).isEqualTo(expectedCount);
    }

    @Test
    @DisplayName("Получение ID пользователя по имени - успешно")
    void getUserIdByUsername_ShouldReturnUserId_WhenUserExists() {
        // Arrange
        String username = EXISTING_USERNAME;
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        // Act
        Long result = commentService.getUserIdByUsername(username);

        // Assert
        assertThat(result).isEqualTo(testUser.getId());
    }

    @Test
    @DisplayName("Получение ID пользователя по имени - пользователь не найден")
    void getUserIdByUsername_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        String username = NON_EXISTING_USERNAME;
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(messageProvider.getMessage("user.not_found")).thenReturn("Пользователь не найден");

        // Act & Assert
        assertThatThrownBy(() -> commentService.getUserIdByUsername(username)).isInstanceOf(EntityNotFoundException.class).hasMessage("Пользователь не найден");
    }

    @Test
    @DisplayName("Создание комментария - пустой контент")
    void createCommentForRecipe_ShouldThrowException_WhenContentIsEmpty() {
        // Arrange
        String content = "   ";

        when(messageProvider.getMessage("comment.content.empty"))
                .thenReturn("Текст комментария не может быть пустым");

        assertThatThrownBy(() -> commentService.createCommentForRecipe(
                content,
                EXISTING_USERNAME,
                EXISTING_RECIPE_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Текст комментария не может быть пустым");

        verify(commentRepository, never()).save(any(Comment.class));
    }
}