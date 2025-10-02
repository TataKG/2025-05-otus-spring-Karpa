package ru.otus.hw.controllers.pages;

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
public class BookPagesController {
    private final BookService bookService;

    private final GenreService genreService;

    private final AuthorService authorService;

    private final CommentService commentService;

    private final BookDtoConverter bookConverter;

    @GetMapping({"/", "/books"})
    public String getList(Model model) {
        return "book-list";
    }

    @GetMapping("/books/view/{id}")
    public String viewPage(@PathVariable String id, Model model) {
        return "book-view";
    }

    @GetMapping({"/books/edit/{id}", "/books/new"})
    public String editPage(@PathVariable(required = false) String id, Model model) {
         return "book-edit";
    }

}
