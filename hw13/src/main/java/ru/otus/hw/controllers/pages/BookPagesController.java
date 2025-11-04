package ru.otus.hw.controllers.pages;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;

@Controller
@RequiredArgsConstructor
public class BookPagesController {
    private final BookService bookService;
    private final CommentService commentService;

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

    @GetMapping("/books/view/{id}")
    public String getBookDetails(@PathVariable Long id, Model model) {
        var book = bookService.findById(id);
        if (book.isEmpty()) {
            return "redirect:/books";
        }
        var comments = commentService.findByBookId(id);

        // Для отладки
        //System.out.println("Book: " + book.get());
        //System.out.println("Comments count: " + comments.size());

        model.addAttribute("book", book.get());
        model.addAttribute("comments", comments);
        return "book-view";
    }
}
