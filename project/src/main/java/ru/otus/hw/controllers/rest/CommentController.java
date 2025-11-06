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
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;
    private final MessageProvider messageProvider;

    public CommentController(CommentService commentService, MessageProvider messageProvider) {
        this.commentService = commentService;
        this.messageProvider = messageProvider;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CommentDto>> createComment(@RequestBody CreateCommentRequest request) {
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
    public ResponseEntity<ApiResponse<CommentDto>> getCommentById(@PathVariable Long id) {
        CommentDto commentDto = commentService.getCommentById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("comment.not_found", id)
                ));
        return ResponseEntity.ok(ApiResponse.success(commentDto));
    }

    @GetMapping("/recipe/{recipeId}")
    public ResponseEntity<ApiResponse<List<CommentDto>>> getCommentsByRecipe(@PathVariable Long recipeId) {
        List<CommentDto> comments = commentService.getCommentsByRecipe(recipeId);
        return ResponseEntity.ok(ApiResponse.success(comments));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<CommentDto>>> getCommentsByUser(@PathVariable Long userId) {
        List<CommentDto> comments = commentService.getCommentsByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(comments));
    }

    @GetMapping("/recipe/{recipeId}/count")
    public ResponseEntity<ApiResponse<Integer>> getCommentCountForRecipe(@PathVariable Long recipeId) {
        int count = commentService.getCommentCountForRecipe(recipeId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok(
                ApiResponse.success(null, messageProvider.getMessage("comment.deleted"))
        );
    }

    public record CreateCommentRequest(String content, Long userId, Long recipeId) {}
}
