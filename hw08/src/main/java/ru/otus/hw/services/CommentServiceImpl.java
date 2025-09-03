package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.converters.CommentDtoConverter;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Comment;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;

    private final BookRepository bookRepository;

    private final CommentDtoConverter commentDtoConverter;

    @Override
    public Optional<CommentDto> findById(String id) {
        if (!commentRepository.existsById(id)) {
            throw new EntityNotFoundException("Comment with id %s not found".formatted(id));
        }
        return commentRepository.findById(id).map(commentDtoConverter::toDto);
    }

    @Override
    public List<CommentDto> findByBookId(String bookId) {
        validateBookExists(bookId);
        return commentRepository.findByBookId(bookId).stream()
                .map(commentDtoConverter::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto insert(String text, String bookId) {
        var comment = new Comment();
        comment.setText(text);
        comment.setBookId(bookId);
        return commentDtoConverter.toDto(commentRepository.save(comment));
    }

    @Override
    public CommentDto update(String id, String text) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment with id %s not found".formatted(id)));
        comment.setText(text);
        return commentDtoConverter.toDto(commentRepository.save(comment));
    }

    @Override
    public void deleteById(String id) {
        if (!commentRepository.existsById(id)) {
            throw new EntityNotFoundException("Comment with id %s not found".formatted(id));
        }
        commentRepository.deleteById(id);
    }

    private void validateBookExists(String bookId) throws EntityNotFoundException {
        if (!bookRepository.existsById(bookId)) {
            throw new EntityNotFoundException("Book with id %s not found".formatted(bookId));
        }
    }

}
