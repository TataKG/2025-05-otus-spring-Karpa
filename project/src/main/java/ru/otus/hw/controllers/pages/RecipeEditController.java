package ru.otus.hw.controllers.pages;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.RecipeDto;
import ru.otus.hw.dto.UserDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.CategoryService;
import ru.otus.hw.services.InventoryService;
import ru.otus.hw.services.RecipeService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/recipe")
@RequiredArgsConstructor
public class RecipeEditController {

    private final RecipeService recipeService;
    private final CategoryService categoryService;
    private final InventoryService inventoryService;
    private final AuthorService authorService;

    @GetMapping("/create")
    public String createRecipePage(Model model, Authentication authentication) {
        String username = authentication.getName();

        AuthorDto author = authorService.getAuthorByUsername(username)
                .orElseGet(() -> {
                    // Если автора нет, создаем временного
                    return new AuthorDto(
                            null,
                            new UserDto(null, username, username + "@example.com", Set.of("ROLE_USER")),
                            "Автор: " + username,
                            LocalDateTime.now(),
                            0
                    );
                });

        RecipeDto emptyRecipe = new RecipeDto(
                null,
                "",
                null,
                author,
                new ArrayList<>(),
                new ArrayList<>(),
                "",
                0,
                false,
                null,
                null
        );

        model.addAttribute("recipe", emptyRecipe);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("inventoryItems", inventoryService.getAllInventory());
        model.addAttribute("isEdit", false);

        return "recipe-edit";
    }

    @GetMapping("/edit/{id}")
    public String editRecipePage(@PathVariable Long id, Model model, Authentication authentication) {
        String username = authentication.getName();

        // Проверяем, что пользователь является автором рецепта
        RecipeDto recipe = recipeService.getRecipeByIdWithAllRelations(id)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found"));

        AuthorDto currentAuthor = authorService.getAuthorByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Author not found"));

        if (!recipe.author().id().equals(currentAuthor.id())) {
            throw new AccessDeniedException("You can only edit your own recipes");
        }

        model.addAttribute("recipe", recipe);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("inventoryItems", inventoryService.getAllInventory());
        model.addAttribute("isEdit", true);

        return "recipe-edit";
    }

    @PostMapping("/save")
    public String saveRecipe(
            @ModelAttribute RecipeDto recipeDto,
            @RequestParam(required = false) List<Long> inventoryIds,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            String username = authentication.getName();
            AuthorDto currentAuthor = authorService.getAuthorByUsername(username)
                    .orElseThrow(() -> new EntityNotFoundException("Author not found"));

            // Для нового рецепта
            if (recipeDto.id() == null) {
                RecipeDto createdRecipe = recipeService.createRecipe(
                        recipeDto.title(),
                        recipeDto.category().id(),
                        currentAuthor.id(),
                        recipeDto.ingredients(),
                        recipeDto.description(),
                        recipeDto.published()
                );

                // Добавляем инвентарь если указан
                if (inventoryIds != null && !inventoryIds.isEmpty()) {
                    recipeService.addInventoryToRecipe(createdRecipe.id(), inventoryIds);
                }

                redirectAttributes.addFlashAttribute("success", "Рецепт успешно создан!");
            } else {
                // Для существующего рецепта - проверяем права
                RecipeDto existingRecipe = recipeService.getRecipeById(recipeDto.id())
                        .orElseThrow(() -> new EntityNotFoundException("Recipe not found"));

                if (!existingRecipe.author().id().equals(currentAuthor.id())) {
                    throw new AccessDeniedException("You can only edit your own recipes");
                }

                RecipeDto updatedRecipe = recipeService.updateRecipe(
                        recipeDto.id(),
                        recipeDto.title(),
                        recipeDto.category().id(),
                        recipeDto.ingredients(),
                        recipeDto.description(),
                        inventoryIds != null ? inventoryIds : new ArrayList<>(),
                        recipeDto.published()
                );

                redirectAttributes.addFlashAttribute("success", "Рецепт успешно обновлен!");
            }

            return "redirect:/my-recipes";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при сохранении рецепта: " + e.getMessage());
            return recipeDto.id() == null ? "redirect:/recipe/create" : "redirect:/recipe/edit/" + recipeDto.id();
        }
    }
}