package ru.otus.hw.controllers.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.otus.hw.converters.BookDtoConverter;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookFormDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.BookService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

    private final BookDtoConverter bookConverter;

    @GetMapping
    public List<BookDto> getAllBooks() {
        return bookService.findAll();
    }

    @GetMapping("/{id}")
    public BookDto getBookById(@PathVariable String id) {
        return bookService.findById(id)
                .orElseThrow(
                        () -> new EntityNotFoundException("Book with id %s not found".formatted(id))
                );
    }


    @PostMapping
    public ResponseEntity<?> createBook(
            @Valid @RequestBody BookFormDto bookDto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body(bindingResult.getAllErrors());
        }

        var savedBook = bookService.insert(bookDto);
        return ResponseEntity.ok(savedBook);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBook(@PathVariable String id,
                                        @Valid @RequestBody BookFormDto bookDto,
                                        BindingResult bindingResult) throws BadRequestException {
        if (!id.equals(bookDto.id())) {
            throw new BadRequestException("ID in path and body must match");
        }

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body(bindingResult.getAllErrors());
        }

        BookDto savedBook = bookService.update(bookDto);
        return ResponseEntity.ok().body(savedBook);
    }

    @DeleteMapping("/{id}")
    public void deleteBook(@PathVariable("id") String id) {
        bookService.deleteById(id);
    }
}
