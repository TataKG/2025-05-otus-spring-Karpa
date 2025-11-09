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

    // ✅ Базовый метод без информации о правах
    public CommentDto toDto(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getContent(),
                comment.getUser() != null ? UserDto.fromEntity(comment.getUser()) : null,
                comment.getRecipe() != null ? comment.getRecipe().getId() : null,
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                false,
                false
        );
    }

    // ✅ Метод с учетом текущего пользователя
    public CommentDto toDto(Comment comment, Long currentUserId) {
        boolean isOwner = false;

        if (currentUserId != null && comment.getUser() != null) {
            // ✅ Сравниваем примитивные long значения
            isOwner = comment.getUser().getId() == currentUserId.longValue();
        }

        return new CommentDto(
                comment.getId(),
                comment.getContent(),
                comment.getUser() != null ? UserDto.fromEntity(comment.getUser()) : null,
                comment.getRecipe() != null ? comment.getRecipe().getId() : null,
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                isOwner,
                isOwner
        );
    }

    // ✅ Метод для списка комментариев
    public List<CommentDto> toDtoList(List<Comment> comments, Long currentUserId) {
        return comments.stream()
                .map(comment -> toDto(comment, currentUserId))
                .collect(Collectors.toList());
    }
}
