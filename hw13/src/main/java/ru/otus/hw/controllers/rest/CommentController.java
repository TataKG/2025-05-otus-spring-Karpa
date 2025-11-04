package ru.otus.hw.controllers.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public List<CommentDto> getCommentsByBookId(@PathVariable("bookId") Long bookId) {
        return commentService.findByBookId(bookId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CommentDto> createComment(@PathVariable("bookId") Long bookId,
                                                    @RequestBody CommentDto commentDto) {
        var savedComment = commentService.insert(commentDto);
        return ResponseEntity.ok().body(savedComment);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable("bookId") Long bookId,
                                              @PathVariable("commentId") Long commentId) {
        commentService.deleteById(commentId);
        return ResponseEntity.noContent().build();
    }
}