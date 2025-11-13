package ru.otus.hw.converters;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.UserDto;
import ru.otus.hw.models.Comment;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CommentConverter {

    private final UserConverter userConverter;

    public CommentDto toDto(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getContent(),
                comment.getUser() != null ? userConverter.toDto(comment.getUser()) : null, // ✅ Использовать конвертер
                comment.getRecipe() != null ? comment.getRecipe().getId() : null,
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                false,
                false
        );
    }

    public CommentDto toDto(Comment comment, Long currentUserId) {
        boolean isOwner = currentUserId != null &&
                comment.getUser() != null &&
                comment.getUser().getId().equals(currentUserId);

        return new CommentDto(
                comment.getId(),
                comment.getContent(),
                comment.getUser() != null ? userConverter.toDto(comment.getUser()) : null,
                comment.getRecipe() != null ? comment.getRecipe().getId() : null,
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                isOwner,
                isOwner
        );
    }

    public List<CommentDto> toDtoList(List<Comment> comments, Long currentUserId) {
        return comments.stream()
                .map(comment -> toDto(comment, currentUserId))
                .collect(Collectors.toList());
    }
}
