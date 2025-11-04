package ru.otus.hw.controllers.pages;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import ru.otus.hw.services.BookService;

@Controller
@RequiredArgsConstructor
public class BookPagesController {
    private final BookService bookService;

    @GetMapping({"/", "/books"})
    public String getList(Model model) {
        var books = bookService.findAll();
        model.addAttribute("books", books);
        return "book-list";
    }

    @GetMapping({"/books/edit/{id}", "/books/new"})
    public String editPage(@PathVariable(required = false) Long id, Model model) {
        return "book-edit";
    }
}
