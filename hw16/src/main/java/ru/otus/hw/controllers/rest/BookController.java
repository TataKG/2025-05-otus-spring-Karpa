package ru.otus.hw.controllers.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookFormDto;
import ru.otus.hw.metrics.ApplicationMetrics;
import ru.otus.hw.services.BookService;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;
    private final ApplicationMetrics metrics;

    @GetMapping
    public List<BookDto> getAllBooks() {
        log.debug("Getting all books");
        return bookService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookDto> getBookById(@PathVariable String id) {
        log.debug("Getting book by id: {}", id);
        Optional<BookDto> book = bookService.findById(id);
        return book.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<BookDto> createBook(@RequestBody BookFormDto bookFormDto) {
        log.info("Creating new book: {}", bookFormDto.title());
        try {
            BookDto savedBook = bookService.insert(bookFormDto);
            metrics.incrementBookCreated();
            log.info("Book created successfully with id: {}", savedBook.id());
            return ResponseEntity.ok(savedBook);
        } catch (Exception e) {
            log.error("Error creating book: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("{id}")
    public ResponseEntity<BookDto> updateBook(@RequestBody BookFormDto bookFormDto) {
        log.info("Updating book with id: {}", bookFormDto.id());
        try {
            BookDto updatedBook = bookService.update(bookFormDto);
            metrics.incrementBookUpdated();
            log.info("Book updated successfully: {}", updatedBook.id());
            return ResponseEntity.ok(updatedBook);
        } catch (Exception e) {
            log.error("Error updating book: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable String id) {
        log.info("Deleting book with id: {}", id);
        try {
            bookService.deleteById(id);
            metrics.incrementBookDeleted();
            log.info("Book deleted successfully: {}", id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting book: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }
}
