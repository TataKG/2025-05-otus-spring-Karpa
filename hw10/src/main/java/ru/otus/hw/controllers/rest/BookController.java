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
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.GenreService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;
    private final AuthorService authorService;
    private final GenreService genreService;

    private final BookDtoConverter bookConverter;

    @GetMapping
    public List<BookDto> getAllBooks() {
        return bookService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookFormDto> getBookById(@PathVariable String id) {
        return bookService.findById(id)
                .map(bookConverter::bookDtoToBookFormDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new EntityNotFoundException("Book with id %s not found".formatted(id)));
    }

    @GetMapping("/form-data")
    public Map<String, Object> getFormData() {
        Map<String, Object> data = new HashMap<>();
        data.put("authors", authorService.findAll());
        data.put("genres", genreService.findAll());
        return data;
    }

//    @GetMapping("/{id}")
//    public ResponseEntity<Book> getBook(@PathVariable String id) {
//        return bookService.findById(id)
//                .map(ResponseEntity::ok)
//                .orElse(ResponseEntity.notFound().build());
//    }

//    // Создать новую книгу
//    @PostMapping
//    public ResponseEntity<Book> createBook(@RequestBody Book book) {
//        Book savedBook = bookService.save(book);
//        return ResponseEntity.ok(savedBook);
//    }

    // Обновить книгу
//    @PutMapping("/{id}")
//    public ResponseEntity<Book> updateBook(@PathVariable String id, @RequestBody Book book) {
//        book.setId(id);
//        Book updatedBook = bookService.save(book);
//        return ResponseEntity.ok(updatedBook);
//    }

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
                                        @RequestBody BookFormDto bookDto,
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

    //Worked
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable String id) {
        try {
            bookService.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
