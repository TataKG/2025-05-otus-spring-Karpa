package ru.otus.hw.controllers.pages;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AuthorPagesController {
    @GetMapping("/authors")
    public String getAuthorList(Model model) {
        return "author-list";
    }
}