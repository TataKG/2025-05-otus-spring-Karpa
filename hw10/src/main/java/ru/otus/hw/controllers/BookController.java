package ru.otus.hw.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import ru.otus.hw.converters.BookDtoConverter;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookFormDto;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.GenreService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

    private final GenreService genreService;

    private final AuthorService authorService;

    private final CommentService commentService;

    private final BookDtoConverter bookConverter;

    @GetMapping({"/", "/books"})
    public String getList(Model model) {
        var books = bookService.findAll();
        model.addAttribute("books", books);
        return "book-list";
    }

    @GetMapping("/books/view/{id}")
    public String viewPage(@PathVariable String id, Model model) {
        BookDto bookDto = bookService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
        List<CommentDto> bookComments = commentService.findByBookId(bookDto.id());

        model.addAttribute("book", bookDto);
        model.addAttribute("comments", bookComments);

        return "book-view";
    }

    @GetMapping({"/books/edit/{id}", "/books/new"})
    public String editPage(@PathVariable(required = false) String id, Model model) {
        BookDto book = (id != null)
                ? bookService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"))
                : new BookDto(null, null, null, null);

        model.addAttribute(
                "book",
                bookConverter.bookDtoToBookFormDto(book));

        model.addAttribute("allAuthors", authorService.findAll());
        model.addAttribute("allGenres", genreService.findAll());

        return "book-edit";
    }

    @PostMapping("/books")
    public String saveBook(@ModelAttribute("book") BookFormDto bookDto) {
        BookDto savedBook = (bookDto.id() != null)
                ? bookService.update(bookDto)
                : bookService.insert(bookDto);

        return "redirect:/books/view/" + savedBook.id();
    }

    @PostMapping("/books/delete")
    public String deleteBook(@RequestParam("bookId") String id) {
        bookService.deleteById(id);
        return "redirect:/books";
    }
}
