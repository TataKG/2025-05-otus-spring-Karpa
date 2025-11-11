package ru.otus.hw.controllers.pages;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/recipe")
@RequiredArgsConstructor
public class RecipePageController {
    @GetMapping("/create")
    public String createRecipePage() {
        return "recipe-edit";
    }

    @GetMapping("/edit/{id}")
    public String editRecipePage(@PathVariable Long id) {
        return "recipe-edit";
    }
}