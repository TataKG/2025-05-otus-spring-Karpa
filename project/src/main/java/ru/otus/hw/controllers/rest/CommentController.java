package ru.otus.hw.controllers.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.hw.dto.ApiResponse;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.RecipeDto;
import ru.otus.hw.dto.UserDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
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
                        .body(ApiResponse.error(messageProvider.getMessage("auth.required")));
            }

            String username = authentication.getName();
            UserDto currentUser = userService.getUserByUsername(username)
                    .orElseThrow(() -> new EntityNotFoundException(
                            messageProvider.getMessage("user.not_found")
                    ));

            RecipeDto recipe = recipeService.getRecipeById(recipeId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            messageProvider.getMessage("recipe.not_found")
                    ));

            if (!recipe.published()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(messageProvider.getMessage("comment.unpublished_recipe")));
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
                    .body(ApiResponse.error(messageProvider.getMessage("comment.create_error") + e.getMessage()));
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
                    .body(ApiResponse.error(messageProvider.getMessage("comment.load_error") + e.getMessage()));
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
                        .body(ApiResponse.error(messageProvider.getMessage("auth.required")));
            }

            String username = authentication.getName();
            UserDto currentUser = userService.getUserByUsername(username)
                    .orElseThrow(() -> new EntityNotFoundException(
                            messageProvider.getMessage("user.not_found")
                    ));

            CommentDto updatedComment = commentService.updateComment(
                    commentId,
                    request.content(),
                    currentUser.id()
            );

            return ResponseEntity.ok(ApiResponse.success(updatedComment, messageProvider.getMessage("comment.updated")));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(messageProvider.getMessage("comment.update_error") + e.getMessage()));
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
                        .body(ApiResponse.error(messageProvider.getMessage("auth.required")));
            }

            String username = authentication.getName();
            UserDto currentUser = userService.getUserByUsername(username)
                    .orElseThrow(() -> new EntityNotFoundException(
                            messageProvider.getMessage("user.not_found")
                    ));

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
                    .body(ApiResponse.error(messageProvider.getMessage("comment.delete_error") + e.getMessage()));
        }
    }

    public record CreateCommentRequest(String content) {
    }

    public record UpdateCommentRequest(String content) {
    }
}