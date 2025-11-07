package ru.otus.hw.services;

import ru.otus.hw.dto.CommentDto;

import java.util.List;
import java.util.Optional;

public interface CommentService {
    CommentDto createComment(String content, Long userId, Long recipeId);

    Optional<CommentDto> getCommentById(Long id);

    List<CommentDto> getCommentsByRecipe(Long recipeId);

    List<CommentDto> getCommentsByRecipeId(Long recipeId);

    List<CommentDto> getCommentsByUser(Long userId);

    int getCommentCountForRecipe(Long recipeId);

    void deleteComment(Long commentId);
}
