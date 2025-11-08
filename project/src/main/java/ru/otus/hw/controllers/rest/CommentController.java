package ru.otus.hw.controllers.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.otus.hw.dto.ApiResponse;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.util.MessageProvider;

import java.util.List;

@RestController
@RequestMapping("/api/recipes/{recipeId}/comments")
public class CommentController {

    private final CommentService commentService;
    private final MessageProvider messageProvider;

    public CommentController(CommentService commentService, MessageProvider messageProvider) {
        this.commentService = commentService;
        this.messageProvider = messageProvider;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CommentDto>> createComment(
            @PathVariable Long recipeId,
            @RequestBody CreateCommentRequest request) {

        if (!recipeId.equals(request.recipeId())) {
            throw new IllegalArgumentException("Recipe ID in path doesn't match request body");
        }

        CommentDto commentDto = commentService.createComment(
                request.content(),
                request.userId(),
                request.recipeId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(commentDto, messageProvider.getMessage("comment.created"))
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CommentDto>> getCommentById(
            @PathVariable Long recipeId,
            @PathVariable Long id) {

        CommentDto commentDto = commentService.getCommentById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("comment.not_found", id)
                ));

        if (!commentDto.recipeId().equals(recipeId)) {
            throw new EntityNotFoundException("Comment not found for this recipe");
        }

        return ResponseEntity.ok(ApiResponse.success(commentDto));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CommentDto>>> getCommentsByRecipe(@PathVariable Long recipeId) {
        List<CommentDto> comments = commentService.getCommentsByRecipe(recipeId);
        return ResponseEntity.ok(ApiResponse.success(comments));
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Integer>> getCommentCountForRecipe(@PathVariable Long recipeId) {
        int count = commentService.getCommentCountForRecipe(recipeId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long recipeId,
            @PathVariable Long commentId) {

        commentService.deleteComment(commentId);
        return ResponseEntity.ok(
                ApiResponse.success(null, messageProvider.getMessage("comment.deleted"))
        );
    }

    public record CreateCommentRequest(String content, Long userId, Long recipeId) {}
}
