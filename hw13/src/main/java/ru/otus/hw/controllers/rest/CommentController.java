package ru.otus.hw.controllers.rest;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.services.CommentService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/books/{bookId}/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @GetMapping
    public List<CommentDto> getCommentsByBookId(@PathVariable("bookId") String bookId) {
        return commentService.findByBookId(bookId);
    }

    @PostMapping
    public ResponseEntity<CommentDto> createComment(@PathVariable("bookId") String bookId,
                                                    @RequestBody CommentDto commentDto) throws BadRequestException {
        if (bookId == null || commentDto == null) {
            throw new BadRequestException("Id is null or empty");
        }
        if (StringUtils.isEmpty(commentDto.text())) {
            throw new BadRequestException("Text is null or empty");
        }
        var savedComment = commentService.insert(commentDto);
        return ResponseEntity.ok().body(savedComment);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable("bookId") String bookId,
                                              @PathVariable("commentId") String commentId) {
        commentService.deleteById(commentId);
        return ResponseEntity.noContent().build();
    }
}