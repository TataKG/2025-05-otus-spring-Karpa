package ru.otus.hw.services;

import ru.otus.hw.dto.CommentDto;

import java.util.List;
import java.util.Optional;

public interface CommentService {
    CommentDto createComment(String content, Long userId, Long recipeId);

    CommentDto updateComment(Long commentId, String content, Long currentUserId);

    void deleteComment(Long commentId, Long currentUserId);

    Optional<CommentDto> getCommentById(Long id);

    Optional<CommentDto> getCommentById(Long id, Long currentUserId);

    List<CommentDto> getCommentsByRecipe(Long recipeId);

    List<CommentDto> getCommentsByRecipe(Long recipeId, Long currentUserId);

    List<CommentDto> getCommentsByRecipeId(Long recipeId);

    List<CommentDto> getCommentsByUser(Long userId);

    int getCommentCountForRecipe(Long recipeId);
}
