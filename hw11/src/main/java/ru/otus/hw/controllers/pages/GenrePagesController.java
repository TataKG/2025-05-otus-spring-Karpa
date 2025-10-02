package ru.otus.hw.controllers.pages;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.otus.hw.services.GenreService;

@Controller
@RequiredArgsConstructor
public class GenrePagesController {
    private final GenreService genreService;

    @GetMapping("/genres")
    public String getGenreList(Model model) {
        return "genre-list";
    }
}
