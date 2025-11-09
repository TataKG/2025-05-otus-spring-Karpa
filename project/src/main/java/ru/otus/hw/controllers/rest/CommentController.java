package ru.otus.hw.controllers.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.otus.hw.dto.*;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.RecipeService;
import ru.otus.hw.services.UserService;
import ru.otus.hw.util.MessageProvider;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recipes/{recipeId}/comments")
public class CommentController {

    private final CommentService commentService;
    private final MessageProvider messageProvider;
    private final RecipeService recipeService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<CommentDto>> createComment(
            @PathVariable Long recipeId,
            @RequestBody CreateCommentRequest request,
            Authentication authentication) {

        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Требуется авторизация для создания комментариев"));
            }

            String username = authentication.getName();
            UserDto currentUser = userService.getUserByUsername(username)
                    .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

            RecipeDto recipe = recipeService.getRecipeById(recipeId)
                    .orElseThrow(() -> new EntityNotFoundException("Рецепт не найден"));

            if (!recipe.published()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Нельзя комментировать неопубликованные рецепты"));
            }

            CommentDto commentDto = commentService.createComment(
                    request.content(),
                    currentUser.id(),
                    recipeId
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.success(commentDto, messageProvider.getMessage("comment.created"))
            );
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Ошибка при создании комментария: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CommentDto>>> getCommentsByRecipe(
            @PathVariable Long recipeId,
            Authentication authentication) {

        try {
            Long currentUserId = null;
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                currentUserId = userService.getUserByUsername(username)
                        .map(UserDto::id)
                        .orElse(null);
            }

            Optional<RecipeDto> recipeOpt = recipeService.getRecipeById(recipeId);
            if (recipeOpt.isEmpty() || !recipeOpt.get().published()) {
                return ResponseEntity.ok(ApiResponse.success(List.of()));
            }

            List<CommentDto> comments = commentService.getCommentsByRecipe(recipeId, currentUserId);
            return ResponseEntity.ok(ApiResponse.success(comments));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Ошибка при загрузке комментариев: " + e.getMessage()));
        }
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentDto>> updateComment(
            @PathVariable Long recipeId,
            @PathVariable Long commentId,
            @RequestBody UpdateCommentRequest request,
            Authentication authentication) {

        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Требуется авторизация"));
            }

            String username = authentication.getName();
            UserDto currentUser = userService.getUserByUsername(username)
                    .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

            CommentDto updatedComment = commentService.updateComment(
                    commentId,
                    request.content(),
                    currentUser.id()
            );

            return ResponseEntity.ok(ApiResponse.success(updatedComment, "Комментарий обновлен"));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Ошибка при обновлении комментария: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long recipeId,
            @PathVariable Long commentId,
            Authentication authentication) {

        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Требуется авторизация"));
            }

            String username = authentication.getName();
            UserDto currentUser = userService.getUserByUsername(username)
                    .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

            commentService.deleteComment(commentId, currentUser.id());

            return ResponseEntity.ok(
                    ApiResponse.success(null, messageProvider.getMessage("comment.deleted"))
            );

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Ошибка при удалении комментария: " + e.getMessage()));
        }
    }

    public record CreateCommentRequest(String content) {
    }

    public record UpdateCommentRequest(String content) {
    }
}