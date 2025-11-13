package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.yml")
@Transactional
@DisplayName("Интеграционные тесты сервисов кулинарной книги")
class IntegrationServicesTest {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthorService authorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private InventoryService inventoryService;

    @Test
    @DisplayName("Полный сценарий: создание пользователя -> автора -> рецепта -> комментария")
    void fullUserAuthorRecipeCommentScenario_ShouldWorkCorrectly() {
        // 1. Используем существующего пользователя
        var userOpt = userService.getUserByUsername("chef_ivan");
        if (userOpt.isEmpty()) {
            userOpt = userService.getUserByUsername("admin");
        }
        assertTrue(userOpt.isPresent(), "Должен существовать хотя бы один пользователь в тестовых данных");
        var userDto = userOpt.get();

        // 2. Используем существующего автора
        var authorOpt = authorService.getAuthorByUserId(userDto.id());
        assertTrue(authorOpt.isPresent(), "Автор должен существовать для пользователя с ID: " + userDto.id());
        var authorDto = authorOpt.get();

        // 3. Создание рецепта - используем первую доступную категорию
        var categories = categoryService.getAllCategories();
        assertFalse(categories.isEmpty(), "Должна существовать хотя бы одна категория");
        var category = categories.get(0);

        var recipeDto = recipeService.createRecipe(
                "Новый рецепт пасты",
                category.id(),
                authorDto.id(),
                List.of("Спагетти - 200г", "Помидоры - 2шт", "Чеснок - 2 зубчика", "Оливковое масло - 2 ст.л."),
                "Простой и вкусный рецепт пасты с томатным соусом и чесноком. Идеально для быстрого ужина.",
                true
        );

        assertNotNull(recipeDto);
        assertEquals("Новый рецепт пасты", recipeDto.title());
        assertTrue(recipeDto.published());

        // 4. Добавление комментария - используем существующего пользователя
        var commentUserOpt = userService.getUserByUsername("baker_maria");
        if (commentUserOpt.isEmpty()) {
            commentUserOpt = userService.getAllEnabledUsers().stream().findFirst();
        }
        assertTrue(commentUserOpt.isPresent(), "Должен существовать пользователь для комментария");
        var commentUser = commentUserOpt.get();

        var commentDto = commentService.createCommentForRecipe(
                "Отличный рецепт! Обязательно попробую.",
                commentUser.username(),
                recipeDto.id()
        );

        assertNotNull(commentDto);
        assertEquals("Отличный рецепт! Обязательно попробую.", commentDto.content());

        // 5. Проверка, что комментарий появился у рецепта
        var comments = commentService.getCommentsForRecipe(recipeDto.id(), commentUser.username());
        assertFalse(comments.isEmpty());
        assertEquals(1, comments.size());
    }

    @Test
    @DisplayName("Сценарий публикации/снятия с публикации рецепта")
    void recipePublishUnpublishScenario_ShouldWorkCorrectly() {
        // 1. Находим неопубликованный рецепт (Сырный суп из test.sql)
        var unpublishedRecipeOpt = recipeService.getRecipeById(8L);
        assertTrue(unpublishedRecipeOpt.isPresent());
        var unpublishedRecipe = unpublishedRecipeOpt.get();
        assertFalse(unpublishedRecipe.published());

        // 2. Публикуем рецепт
        var publishedRecipe = recipeService.publishRecipe(8L);
        assertTrue(publishedRecipe.published());

        // 3. Проверяем, что рецепт появился в опубликованных
        var publishedRecipes = recipeService.getAllPublishedRecipes();
        assertTrue(publishedRecipes.stream().anyMatch(r -> r.id().equals(8L)));

        // 4. Снимаем с публикации
        var unpublishedAgain = recipeService.unpublishRecipe(8L);
        assertFalse(unpublishedAgain.published());
    }

    @Test
    @DisplayName("Сценарий работы с комментариями")
    void commentsScenario_ShouldWorkCorrectly() {
        // 1. Получение комментариев для рецепта
        var comments = commentService.getCommentsForRecipe(1L, "chef_ivan"); // Борщ
        assertFalse(comments.isEmpty());

        // 2. Получение комментариев пользователя
        var userComments = commentService.getCommentsByUser(2L); // baker_maria
        assertFalse(userComments.isEmpty());

        // 3. Подсчет комментариев
        var commentCount = commentService.getCommentCountForRecipe(1L);
        assertTrue(commentCount > 0);

        // 4. Получение конкретного комментария
        var commentOpt = commentService.getCommentById(1L);
        assertTrue(commentOpt.isPresent());
    }

    @Test
    @DisplayName("Сценарий проверки статистики")
    void statisticsScenario_ShouldWorkCorrectly() {
        // 1. Количество опубликованных рецептов
        var publishedCount = recipeService.getPublishedRecipesCount();
        assertTrue(publishedCount > 0);

        // 2. Категории с информацией об использовании
        var categoriesWithUsage = categoryService.getCategoriesWithUsage();
        assertFalse(categoriesWithUsage.isEmpty());

        // 3. Инвентарь с информацией об использовании
        var inventoryWithUsage = inventoryService.getInventoryWithUsage();
        assertFalse(inventoryWithUsage.isEmpty());

        // 4. Проверка использования категорий в рецептах
        var categoryUsage = categoryService.isCategoryUsedInRecipes(1L); // Супы
        assertTrue(categoryUsage);

        // 5. Количество рецептов по категории
        var recipeCount = categoryService.getRecipeCountByCategory(1L);
        assertTrue(recipeCount > 0);
    }
}
