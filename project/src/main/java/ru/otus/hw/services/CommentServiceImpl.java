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
import ru.otus.hw.util.MessageProvider;

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
    public CommentDto createComment(String content, Long userId, Long recipeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Рецепт не найден"));

        if (!recipe.isPublished()) {
            throw new IllegalStateException("Нельзя комментировать неопубликованные рецепты");
        }

        Comment comment = new Comment(content, user, recipe);
        Comment savedComment = commentRepository.save(comment);

        return commentConverter.toDto(savedComment, userId);
    }

    @Override
    public CommentDto updateComment(Long commentId, String content, Long currentUserId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Комментарий не найден"));

        if (comment.getUser().getId() != currentUserId.longValue()) {
            throw new SecurityException("Вы можете редактировать только свои комментарии");
        }

        comment.setContent(content);
        Comment updatedComment = commentRepository.save(comment);

        return commentConverter.toDto(updatedComment, currentUserId);
    }

    @Override
    public void deleteComment(Long commentId, Long currentUserId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Комментарий не найден"));

        if (comment.getUser().getId() != currentUserId.longValue()) {
            throw new SecurityException("Вы можете удалять только свои комментарии");
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
    public List<CommentDto> getCommentsByRecipe(Long recipeId) {
        return commentRepository.findByRecipeIdWithUser(recipeId).stream()
                .map(commentConverter::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentDto> getCommentsByRecipe(Long recipeId, Long currentUserId) {
        return commentRepository.findByRecipeIdWithUser(recipeId).stream()
                .map(comment -> commentConverter.toDto(comment, currentUserId)) // ✅ С правами
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentDto> getCommentsByRecipeId(Long recipeId) {
        return getCommentsByRecipe(recipeId);
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
}