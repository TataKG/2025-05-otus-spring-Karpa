package ru.otus.hw.controllers.pages;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class BookPagesController {
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
