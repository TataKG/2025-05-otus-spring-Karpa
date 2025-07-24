package ru.otus.hw.converters;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.models.Comment;

@Component
@RequiredArgsConstructor
public class CommentDtoConverter {
    public String commentDtoToString(CommentDto comment) {
        return "Id: %d, text: %s, book id: %d".formatted(comment.id(), comment.text(), comment.bookId());
    }

    public CommentDto toDto(Comment comment) {
        if (comment != null) {
            return new CommentDto(comment.getId(), comment.getText(), comment.getBook().getId());
        } else {
            return null;
        }
    }
}