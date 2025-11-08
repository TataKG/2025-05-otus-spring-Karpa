package ru.otus.hw.controllers.pages;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    @GetMapping
    public String adminPanel() {
        return "admin/admin-panel";  // должен вести на admin-panel.html
    }

    @GetMapping("/categories")
    public String categoriesManagement() {
        return "admin/categories";   // должен вести на categories.html
    }

    @GetMapping("/inventory")
    public String inventoryManagement() {
        return "admin/inventory"; // должен вести на inventory.html
    }

    @GetMapping("/authors")
    public String authorsManagement() {
        return "admin/authors";      // должен вести на authors.html
    }
}
