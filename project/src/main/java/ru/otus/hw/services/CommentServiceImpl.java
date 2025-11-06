package ru.otus.hw.services;

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
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final CommentConverter commentConverter;
    private final MessageProvider messageProvider;

    public CommentServiceImpl(CommentRepository commentRepository,
                              UserRepository userRepository,
                              RecipeRepository recipeRepository,
                              CommentConverter commentConverter,
                              MessageProvider messageProvider) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.recipeRepository = recipeRepository;
        this.commentConverter = commentConverter;
        this.messageProvider = messageProvider;
    }

    @Override
    public CommentDto createComment(String content, Long userId, Long recipeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("user.not_found", userId)
                ));

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("recipe.not_found", recipeId)
                ));

        Comment comment = new Comment(content, user, recipe);
        Comment savedComment = commentRepository.save(comment);
        return commentConverter.toDto(savedComment);
    }

    @Override
    public Optional<CommentDto> getCommentById(Long id) {
        return commentRepository.findByIdWithUserAndRecipe(id)
                .map(commentConverter::toDto);
    }

    @Override
    public List<CommentDto> getCommentsByRecipe(Long recipeId) {
        return commentRepository.findByRecipeIdWithUser(recipeId).stream()
                .map(commentConverter::toDto)
                .collect(Collectors.toList());
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
    public void deleteComment(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new EntityNotFoundException(
                    messageProvider.getMessage("comment.not_found", commentId)
            );
        }
        commentRepository.deleteById(commentId);
    }
}
