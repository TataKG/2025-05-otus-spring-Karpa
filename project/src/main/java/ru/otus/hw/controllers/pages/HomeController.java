package ru.otus.hw.controllers.pages;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/my-recipes")
    public String myRecipes() {
        return "my-recipes";
    }
}