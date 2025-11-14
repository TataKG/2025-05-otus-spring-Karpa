package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.dto.RecipeSummaryDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.yml")
@Transactional
@DisplayName("Интеграционные тесты сервисов кулинарной книги")
class IntegrationServicesTest {

    private static final Long BORSCH_RECIPE_ID = 1L;
    private static final Long UNPUBLISHED_SOUP_RECIPE_ID = 8L;
    private static final Long SOUPS_CATEGORY_ID = 1L;
    private static final Long BAKER_MARIA_USER_ID = 2L;
    private static final Long FIRST_COMMENT_ID = 1L;

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
        assertThat(userOpt).as("Должен существовать хотя бы один пользователь в тестовых данных").isPresent();
        var userDto = userOpt.get();

        // 2. Используем существующего автора
        var authorOpt = authorService.getAuthorByUserId(userDto.id());
        assertThat(authorOpt).as("Автор должен существовать для пользователя с ID: " + userDto.id()).isPresent();
        var authorDto = authorOpt.get();

        // 3. Создание рецепта - используем первую доступную категорию
        var categories = categoryService.getAllCategories();
        assertThat(categories).as("Должна существовать хотя бы одна категория").isNotEmpty();
        var category = categories.get(0);

        var recipeDto = recipeService.createRecipe(
                "Новый рецепт пасты",
                category.id(),
                authorDto.id(),
                List.of("Спагетти - 200г", "Помидоры - 2шт", "Чеснок - 2 зубчика", "Оливковое масло - 2 ст.л."),
                "Простой и вкусный рецепт пасты с томатным соусом и чесноком. Идеально для быстрого ужина.",
                true
        );

        assertThat(recipeDto).isNotNull();
        assertThat(recipeDto.title()).isEqualTo("Новый рецепт пасты");
        assertThat(recipeDto.published()).isTrue();

        // 4. Добавление комментария - используем существующего пользователя
        var commentUserOpt = userService.getUserByUsername("baker_maria");
        if (commentUserOpt.isEmpty()) {
            commentUserOpt = userService.getAllEnabledUsers().stream().findFirst();
        }
        assertThat(commentUserOpt).as("Должен существовать пользователь для комментария").isPresent();
        var commentUser = commentUserOpt.get();

        var commentDto = commentService.createCommentForRecipe(
                "Отличный рецепт! Обязательно попробую.",
                commentUser.username(),
                recipeDto.id()
        );

        assertThat(commentDto).isNotNull();
        assertThat(commentDto.content()).isEqualTo("Отличный рецепт! Обязательно попробую.");

        // 5. Проверка, что комментарий появился у рецепта
        var comments = commentService.getCommentsForRecipe(recipeDto.id(), commentUser.username());
        assertThat(comments).isNotEmpty();
        assertThat(comments).hasSize(1);
    }

    @Test
    @DisplayName("Сценарий публикации/снятия с публикации рецепта")
    void recipePublishUnpublishScenario_ShouldWorkCorrectly() {
        // 1. Находим неопубликованный рецепт (Сырный суп из test.sql)
        var unpublishedRecipeOpt = recipeService.getRecipeById(UNPUBLISHED_SOUP_RECIPE_ID);
        assertThat(unpublishedRecipeOpt).isPresent();
        var unpublishedRecipe = unpublishedRecipeOpt.get();
        assertThat(unpublishedRecipe.published()).isFalse();

        // 2. Публикуем рецепт
        var publishedRecipe = recipeService.publishRecipe(UNPUBLISHED_SOUP_RECIPE_ID);
        assertThat(publishedRecipe.published()).isTrue();

        // 3. Проверяем, что рецепт появился в опубликованных
        var publishedRecipes = recipeService.getAllPublishedRecipes();
        assertThat(publishedRecipes)
                .extracting(RecipeSummaryDto::id)
                .contains(UNPUBLISHED_SOUP_RECIPE_ID);

        // 4. Снимаем с публикации
        var unpublishedAgain = recipeService.unpublishRecipe(UNPUBLISHED_SOUP_RECIPE_ID);
        assertThat(unpublishedAgain.published()).isFalse();
    }

    @Test
    @DisplayName("Сценарий работы с комментариями")
    void commentsScenario_ShouldWorkCorrectly() {
        // 1. Получение комментариев для рецепта
        var comments = commentService.getCommentsForRecipe(BORSCH_RECIPE_ID, "chef_ivan"); // Борщ
        assertThat(comments).isNotEmpty();

        // 2. Получение комментариев пользователя
        var userComments = commentService.getCommentsByUser(BAKER_MARIA_USER_ID); // baker_maria
        assertThat(userComments).isNotEmpty();

        // 3. Подсчет комментариев
        var commentCount = commentService.getCommentCountForRecipe(BORSCH_RECIPE_ID);
        assertThat(commentCount).isPositive();

        // 4. Получение конкретного комментария
        var commentOpt = commentService.getCommentById(FIRST_COMMENT_ID);
        assertThat(commentOpt).isPresent();
    }

    @Test
    @DisplayName("Сценарий проверки статистики")
    void statisticsScenario_ShouldWorkCorrectly() {
        // 1. Количество опубликованных рецептов
        var publishedCount = recipeService.getPublishedRecipesCount();
        assertThat(publishedCount).isPositive();

        // 2. Категории с информацией об использовании
        var categoriesWithUsage = categoryService.getCategoriesWithUsage();
        assertThat(categoriesWithUsage).isNotEmpty();

        // 3. Инвентарь с информацией об использовании
        var inventoryWithUsage = inventoryService.getInventoryWithUsage();
        assertThat(inventoryWithUsage).isNotEmpty();

        // 4. Проверка использования категорий в рецептах
        var categoryUsage = categoryService.isCategoryUsedInRecipes(SOUPS_CATEGORY_ID); // Супы
        assertThat(categoryUsage).isTrue();

        // 5. Количество рецептов по категории
        var recipeCount = categoryService.getRecipeCountByCategory(SOUPS_CATEGORY_ID);
        assertThat(recipeCount).isPositive();
    }

    @Test
    @DisplayName("Сценарий работы с категориями и инвентарем")
    void categoriesAndInventoryScenario_ShouldWorkCorrectly() {
        // 1. Получение всех категорий
        var allCategories = categoryService.getAllCategories();
        assertThat(allCategories).isNotEmpty();

        // 2. Получение неиспользуемых категорий
        var unusedCategories = categoryService.getUnusedCategories();
        assertThat(unusedCategories).isNotNull();

        // 3. Получение всего инвентаря
        var allInventory = inventoryService.getAllInventory();
        assertThat(allInventory).isNotEmpty();

        // 4. Получение неиспользуемого инвентаря
        var unusedInventory = inventoryService.getUnusedInventory();
        assertThat(unusedInventory).isNotNull();

        // 5. Проверка использования инвентаря в рецептах
        var inventoryUsage = inventoryService.isInventoryUsedInRecipes(1L); // Блендер
        assertThat(inventoryUsage).isTrue();
    }

    @Test
    @DisplayName("Сценарий поиска и фильтрации рецептов")
    void recipeSearchAndFilterScenario_ShouldWorkCorrectly() {
        // 1. Поиск рецептов по тексту - используем "суп" вместо "борщ"
        var searchResults = recipeService.findPublishedRecipesWithFilters("суп", null, null);
        assertThat(searchResults).isNotEmpty();

        // 2. Фильтрация по категории
        var categoryResults = recipeService.findPublishedRecipesWithFilters(null, SOUPS_CATEGORY_ID, null);
        assertThat(categoryResults).isNotEmpty();

        // 3. Фильтрация по автору
        var authorResults = recipeService.findPublishedRecipesWithFilters(null, null, 1L);
        assertThat(authorResults).isNotEmpty();

        // 4. Комбинированная фильтрация
        var combinedResults = recipeService.findPublishedRecipesWithFilters("торт", 5L, 2L);
        assertThat(combinedResults).isNotNull();
    }

    @Test
    @DisplayName("Сценарий работы с пользователями и авторами")
    void usersAndAuthorsScenario_ShouldWorkCorrectly() {
        // 1. Получение всех пользователей
        var allUsers = userService.getAllEnabledUsers();
        assertThat(allUsers).isNotEmpty();

        // 2. Получение пользователя по имени
        var userOpt = userService.getUserByUsername("chef_ivan");
        assertThat(userOpt).isPresent();

        // 3. Получение всех авторов
        var allAuthors = authorService.getAllAuthors();
        assertThat(allAuthors).isNotEmpty();

        // 4. Проверка существования автора
        var authorExists = authorService.existsByUserId(1L);
        assertThat(authorExists).isTrue();

        // 5. Получение автора с информацией о рецептах
        var authorWithRecipesOpt = authorService.getAuthorWithRecipes(1L);
        assertThat(authorWithRecipesOpt).isPresent();
        assertThat(authorWithRecipesOpt.get().recipeCount()).isPositive();
    }
}