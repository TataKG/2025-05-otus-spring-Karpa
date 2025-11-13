package ru.otus.hw.services;

import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.models.Recipe;
import ru.otus.hw.models.User;

import java.util.List;
import java.util.Optional;

public interface CommentService {
    CommentDto createCommentForRecipe(String content, String username, Long recipeId);

    CommentDto updateComment(Long commentId, String content, Long currentUserId);

    void deleteComment(Long commentId, Long currentUserId);

    List<CommentDto> getCommentsForRecipe(Long recipeId, String username);

    Optional<CommentDto> getCommentById(Long id);

    Optional<CommentDto> getCommentById(Long id, Long currentUserId);

    List<CommentDto> getCommentsByUser(Long userId);

    int getCommentCountForRecipe(Long recipeId);

    Long getUserIdByUsername(String username);
}
