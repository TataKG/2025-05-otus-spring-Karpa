package ru.otus.hw.controllers.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.exceptions.BadRequestException;
import ru.otus.hw.services.CommentService;

import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/books/{bookId}/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @GetMapping
    public Flux<CommentDto> getCommentsByBookId(@PathVariable("bookId") String bookId) {
        return commentService.findByBookId(bookId);
    }

    @PostMapping
    public Mono<ResponseEntity<Object>> createComment(@PathVariable("bookId") String bookId,
                                                      @RequestBody Mono<CommentDto> commentDtoMono) throws BadRequestException {

        if (bookId == null || bookId.isBlank()) {
            return Mono.error(new BadRequestException("Book id is null or empty"));
        }

        return commentDtoMono
                .flatMap(commentDto -> commentService.insert(commentDto)
                        .map(savedComment -> ResponseEntity.ok().<Object>body(savedComment))
                )
                .onErrorResume(WebExchangeBindException.class, ex -> {
                    var errors = ex.getFieldErrors().stream()
                            .collect(Collectors.toMap(
                                    FieldError::getField,
                                    fieldError -> Optional.ofNullable(fieldError.getDefaultMessage())
                                            .orElse("Invalid value")
                            ));
                    return Mono.just(ResponseEntity.badRequest().body(errors));
                });
    }

    @DeleteMapping("/{commentId}")
    public Mono<ResponseEntity<Void>> deleteComment(@PathVariable("bookId") String bookId,
                                                    @PathVariable("commentId") String commentId) {
        return commentService.deleteById(commentId)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }
}