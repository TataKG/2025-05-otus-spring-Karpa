package ru.otus.hw.converters;

import org.springframework.stereotype.Component;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.models.Comment;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CommentConverter {

    private final UserConverter userConverter;

    public CommentConverter(UserConverter userConverter) {
        this.userConverter = userConverter;
    }

    public CommentDto toDto(Comment comment) {
        if (comment == null) return null;

        return new CommentDto(
                comment.getId(),
                comment.getContent(),
                userConverter.toDto(comment.getUser()),
                comment.getRecipe() != null ? comment.getRecipe().getId() : null,
                comment.getCreatedAt()
        );
    }

    public List<CommentDto> toDtoList(List<Comment> comments) {
        return comments.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
