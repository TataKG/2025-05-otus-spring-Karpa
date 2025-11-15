package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.converters.CommentConverter;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Recipe;
import ru.otus.hw.models.User;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.RecipeRepository;
import ru.otus.hw.repositories.UserRepository;
import ru.otus.hw.utils.MessageProvider;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final CommentConverter commentConverter;
    private final MessageProvider messageProvider;

    @Override
    public CommentDto createCommentForRecipe(String content, String username, Long recipeId) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    messageProvider.getMessage("comment.content.empty")
            );
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("user.not_found")
                ));

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("recipe.not_found", recipeId)
                ));

        if (!recipe.isPublished()) {
            throw new IllegalStateException(
                    messageProvider.getMessage("comment.unpublished_recipe")
            );
        }

        Comment comment = new Comment(content.trim(), user, recipe);
        Comment savedComment = commentRepository.save(comment);
        return commentConverter.toDto(savedComment, user.getId());
    }

    @Override
    public List<CommentDto> getCommentsForRecipe(Long recipeId, String username) {
        Long currentUserId = username != null
                ? userRepository.findByUsername(username).map(User::getId).orElse(null)
                : null;

        boolean recipeExistsAndPublished = recipeRepository.existsByIdAndPublishedTrue(recipeId);

        if (!recipeExistsAndPublished) {
            return List.of();
        }

        final Long finalCurrentUserId = currentUserId;
        return commentRepository.findByRecipeIdWithUser(recipeId).stream()
                .map(comment -> commentConverter.toDto(comment, finalCurrentUserId))
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto updateComment(Long commentId, String content, Long currentUserId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("comment.not_found", commentId)
                ));

        if (!comment.getUser().getId().equals(currentUserId)) {
            throw new SecurityException(
                    messageProvider.getMessage("comment.edit_denied")
            );
        }

        comment.setContent(content);
        Comment updatedComment = commentRepository.save(comment);
        return commentConverter.toDto(updatedComment, currentUserId);
    }

    @Override
    public void deleteComment(Long commentId, Long currentUserId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("comment.not_found", commentId)
                ));

        if (!comment.getUser().getId().equals(currentUserId)) {
            throw new SecurityException(
                    messageProvider.getMessage("comment.delete_denied")
            );
        }

        commentRepository.delete(comment);
    }

    @Override
    public Optional<CommentDto> getCommentById(Long id) {
        return commentRepository.findByIdWithUserAndRecipe(id)
                .map(commentConverter::toDto);
    }

    @Override
    public Optional<CommentDto> getCommentById(Long id, Long currentUserId) {
        return commentRepository.findByIdWithUserAndRecipe(id)
                .map(comment -> commentConverter.toDto(comment, currentUserId));
    }

    @Override
    public List<CommentDto> getCommentsByUser(Long userId) {
        return commentRepository.findByUserId(userId).stream()
                .map(commentConverter::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public int getCommentCountForRecipe(Long recipeId) {
        return commentRepository.countByRecipeId(recipeId);
    }

    @Override
    public Long getUserIdByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(User::getId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("user.not_found")
                ));
    }

    private Long getCurrentUserId(String username) {
        if (username == null) {
            return null;
        }
        return userRepository.findByUsername(username)
                .map(User::getId)
                .orElse(null);
    }
}