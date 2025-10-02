package ru.otus.hw.controllers.pages;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.otus.hw.services.AuthorService;

@Controller
@RequiredArgsConstructor
public class AuthorPagesController {
    private final AuthorService authorService;

    @GetMapping("/authors")
    public String getAuthorList(Model model) {
        return "author-list";
    }

}
