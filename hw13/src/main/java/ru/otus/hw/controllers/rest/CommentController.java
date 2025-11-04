package ru.otus.hw.controllers.rest;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
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
                                                    @RequestBody CommentDto commentDto) throws BadRequestException {
        System.out.println("Received request to create comment:");
        System.out.println("Book ID: " + bookId);
        System.out.println("Comment DTO: " + commentDto);

        if (bookId == null || commentDto == null) {
            throw new BadRequestException("Id is null or empty");
        }
        if (StringUtils.isEmpty(commentDto.text())) {
            throw new BadRequestException("Text is null or empty");
        }

        try {
            var savedComment = commentService.insert(commentDto);
            System.out.println("Comment saved successfully: " + savedComment);
            return ResponseEntity.ok().body(savedComment);
        } catch (Exception e) {
            System.out.println("Error saving comment: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable("bookId") Long bookId,
                                              @PathVariable("commentId") Long commentId) {
        commentService.deleteById(commentId);
        return ResponseEntity.noContent().build();
    }

    // Добавьте этот метод для тестирования
    @GetMapping("/test")
    public String testEndpoint(@PathVariable("bookId") Long bookId) {
        System.out.println("TEST ENDPOINT CALLED with bookId: " + bookId);
        return "Test successful for book: " + bookId;
    }

}