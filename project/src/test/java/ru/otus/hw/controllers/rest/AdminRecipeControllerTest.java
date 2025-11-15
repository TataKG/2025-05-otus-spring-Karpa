package ru.otus.hw.controllers.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import ru.otus.hw.dto.ApiResponse;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.CategoryDto;
import ru.otus.hw.dto.InventoryDto;
import ru.otus.hw.dto.RecipeDto;
import ru.otus.hw.dto.UserDto;
import ru.otus.hw.services.RecipeService;
import ru.otus.hw.utils.MessageProvider;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты контроллера администратора для работы с рецептами")
class AdminRecipeControllerTest {

    private static final Long EXISTING_RECIPE_ID = 1L;
    private static final Long ANOTHER_RECIPE_ID = 2L;
    private static final Long UNPUBLISHED_RECIPE_ID = 8L;
    private static final Long NON_EXISTING_RECIPE_ID = 999L;
    private static final Long EXISTING_CATEGORY_ID = 1L;
    private static final Long EXISTING_AUTHOR_ID = 1L;

    private static final String BORSCH_TITLE = "Борщ украинский";
    private static final String CAKE_TITLE = "Шоколадный торт";
    private static final String UNPUBLISHED_SOUP_TITLE = "Сырный суп";

    @Mock
    private RecipeService recipeService;

    @Mock
    private MessageProvider messageProvider;

    @InjectMocks
    private AdminRecipeController adminRecipeController;

    @BeforeEach
    void setUp() {
        UserDetails adminUser = new User("admin", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(adminUser, null, adminUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private RecipeDto createBorschRecipe() {
        return createTestRecipe(EXISTING_RECIPE_ID, BORSCH_TITLE, true);
    }

    private RecipeDto createCakeRecipe() {
        return createTestRecipe(ANOTHER_RECIPE_ID, CAKE_TITLE, true);
    }

    private RecipeDto createUnpublishedSoupRecipe() {
        return createTestRecipe(UNPUBLISHED_RECIPE_ID, UNPUBLISHED_SOUP_TITLE, false);
    }

    private RecipeDto createTestRecipe(Long id, String title, boolean published) {
        UserDto userDto = new UserDto(
                EXISTING_AUTHOR_ID,
                "chef_ivan",
                "ivan@example.com",
                true,
                Set.of("USER"),
                LocalDateTime.now(),
                true
        );

        AuthorDto authorDto = new AuthorDto(
                EXISTING_AUTHOR_ID,
                userDto,
                "Профессиональный шеф-повар с 15-летним опытом",
                LocalDateTime.now(),
                5,
                List.of("USER")
        );

        CategoryDto categoryDto = new CategoryDto(
                EXISTING_CATEGORY_ID,
                "Супы",
                "Первый блюда: горячие и холодные супы, бульоны, крем-супы",
                LocalDateTime.now()
        );

        InventoryDto inventoryDto = new InventoryDto(
                1L,
                "Блендер",
                "Кухонный блендер для приготовления супов-пюре и коктейлей",
                LocalDateTime.now()
        );

        return new RecipeDto(
                id,
                title,
                categoryDto,
                authorDto,
                List.of(inventoryDto),
                List.of("Говядина (грудинка) - 500 г", "Свекла - 2 шт.", "Капуста белокочанная - 300 г"),
                "Классический украинский борщ с говядиной и свеклой. Ароматный, наваристый, с характерной кислинкой.",
                2,
                published,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("Получение опубликованных рецептов - успешное выполнение без фильтров")
    void getPublishedRecipes_Success_NoFilters() {
        // Arrange
        RecipeDto borschRecipe = createBorschRecipe();
        RecipeDto cakeRecipe = createCakeRecipe();
        List<RecipeDto> recipes = List.of(borschRecipe, cakeRecipe);
        when(recipeService.findPublishedRecipesWithFilters(null, null, null)).thenReturn(recipes);

        // Act
        ResponseEntity<ApiResponse<List<RecipeDto>>> response = adminRecipeController.getPublishedRecipes(null, null, null);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data()).isEqualTo(recipes);
        assertThat(response.getBody().data()).hasSize(2);
        verify(recipeService).findPublishedRecipesWithFilters(null, null, null);
    }

    @Test
    @DisplayName("Получение опубликованных рецептов - успешное выполнение с поиском по названию")
    void getPublishedRecipes_Success_WithSearch() {
        // Arrange
        String search = "борщ";
        RecipeDto borschRecipe = createBorschRecipe();
        List<RecipeDto> recipes = List.of(borschRecipe);
        when(recipeService.findPublishedRecipesWithFilters(search, null, null)).thenReturn(recipes);

        // Act
        ResponseEntity<ApiResponse<List<RecipeDto>>> response = adminRecipeController.getPublishedRecipes(search, null, null);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data()).isEqualTo(recipes);
        assertThat(response.getBody().data()).hasSize(1);
        assertThat(response.getBody().data().get(0).title()).isEqualTo(BORSCH_TITLE);
        verify(recipeService).findPublishedRecipesWithFilters(search, null, null);
    }

    @Test
    @DisplayName("Получение опубликованных рецептов - успешное выполнение с фильтром по категории")
    void getPublishedRecipes_Success_WithCategoryFilter() {
        // Arrange
        RecipeDto borschRecipe = createBorschRecipe();
        List<RecipeDto> recipes = List.of(borschRecipe);
        when(recipeService.findPublishedRecipesWithFilters(null, EXISTING_CATEGORY_ID, null)).thenReturn(recipes);

        // Act
        ResponseEntity<ApiResponse<List<RecipeDto>>> response = adminRecipeController.getPublishedRecipes(null, EXISTING_CATEGORY_ID, null);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data()).isEqualTo(recipes);
        assertThat(response.getBody().data().get(0).category().id()).isEqualTo(EXISTING_CATEGORY_ID);
        verify(recipeService).findPublishedRecipesWithFilters(null, EXISTING_CATEGORY_ID, null);
    }

    @Test
    @DisplayName("Получение опубликованных рецептов - успешное выполнение с фильтром по автору")
    void getPublishedRecipes_Success_WithAuthorFilter() {
        // Arrange
        RecipeDto borschRecipe = createBorschRecipe();
        List<RecipeDto> recipes = List.of(borschRecipe);
        when(recipeService.findPublishedRecipesWithFilters(null, null, EXISTING_AUTHOR_ID)).thenReturn(recipes);

        // Act
        ResponseEntity<ApiResponse<List<RecipeDto>>> response = adminRecipeController.getPublishedRecipes(null, null, EXISTING_AUTHOR_ID);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data()).isEqualTo(recipes);
        assertThat(response.getBody().data().get(0).author().id()).isEqualTo(EXISTING_AUTHOR_ID);
        verify(recipeService).findPublishedRecipesWithFilters(null, null, EXISTING_AUTHOR_ID);
    }

    @Test
    @DisplayName("Получение опубликованных рецептов - успешное выполнение со всеми фильтрами")
    void getPublishedRecipes_Success_WithAllFilters() {
        // Arrange
        String search = "борщ";
        RecipeDto borschRecipe = createBorschRecipe();
        List<RecipeDto> recipes = List.of(borschRecipe);
        when(recipeService.findPublishedRecipesWithFilters(search, EXISTING_CATEGORY_ID, EXISTING_AUTHOR_ID)).thenReturn(recipes);

        // Act
        ResponseEntity<ApiResponse<List<RecipeDto>>> response = adminRecipeController.getPublishedRecipes(search, EXISTING_CATEGORY_ID, EXISTING_AUTHOR_ID);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data()).isEqualTo(recipes);
        assertThat(response.getBody().data().get(0).title()).isEqualTo(BORSCH_TITLE);
        assertThat(response.getBody().data().get(0).category().id()).isEqualTo(EXISTING_CATEGORY_ID);
        assertThat(response.getBody().data().get(0).author().id()).isEqualTo(EXISTING_AUTHOR_ID);
        verify(recipeService).findPublishedRecipesWithFilters(search, EXISTING_CATEGORY_ID, EXISTING_AUTHOR_ID);
    }

    @Test
    @DisplayName("Получение опубликованных рецептов - пустой результат")
    void getPublishedRecipes_Success_EmptyResult() {
        // Arrange
        String search = "несуществующий рецепт";
        when(recipeService.findPublishedRecipesWithFilters(search, null, null)).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<ApiResponse<List<RecipeDto>>> response = adminRecipeController.getPublishedRecipes(search, null, null);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data()).isEmpty();
        verify(recipeService).findPublishedRecipesWithFilters(search, null, null);
    }

    @Test
    @DisplayName("Получение опубликованных рецептов - возвращаются только опубликованные рецепты")
    void getPublishedRecipes_OnlyPublishedRecipesReturned() {
        // Arrange
        RecipeDto publishedRecipe1 = createBorschRecipe();
        RecipeDto publishedRecipe2 = createCakeRecipe();

        // Создаем неопубликованный рецепт для демонстрации (но он не должен возвращаться сервисом)
        createUnpublishedSoupRecipe(); // Этот рецепт существует в базе, но не публикован

        // Сервис должен возвращать ТОЛЬКО опубликованные рецепты, даже если в базе есть неопубликованные
        List<RecipeDto> publishedRecipes = List.of(publishedRecipe1, publishedRecipe2);
        when(recipeService.findPublishedRecipesWithFilters(null, null, null)).thenReturn(publishedRecipes);

        // Act
        ResponseEntity<ApiResponse<List<RecipeDto>>> response = adminRecipeController.getPublishedRecipes(null, null, null);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data()).hasSize(2);

        // Проверяем, что все возвращенные рецепты опубликованы
        assertThat(response.getBody().data())
                .allMatch(RecipeDto::published)
                .noneMatch(recipe -> !recipe.published());

        // Проверяем, что неопубликованный рецепт отсутствует в результатах
        assertThat(response.getBody().data())
                .extracting(RecipeDto::title)
                .doesNotContain(UNPUBLISHED_SOUP_TITLE);

        verify(recipeService).findPublishedRecipesWithFilters(null, null, null);
    }

    @Test
    @DisplayName("Получение опубликованных рецептов - поиск неопубликованного рецепта возвращает пустой результат")
    void getPublishedRecipes_SearchUnpublishedRecipe_ReturnsEmpty() {
        // Arrange
        String search = "сырный";
        // Даже если ищем по названию неопубликованного рецепта, сервис должен вернуть пустой список
        when(recipeService.findPublishedRecipesWithFilters(search, null, null)).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<ApiResponse<List<RecipeDto>>> response = adminRecipeController.getPublishedRecipes(search, null, null);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data()).isEmpty();
        verify(recipeService).findPublishedRecipesWithFilters(search, null, null);
    }

    @Test
    @DisplayName("Получение опубликованных рецептов - внутренняя ошибка сервера")
    void getPublishedRecipes_InternalServerError() {
        // Arrange
        String errorMessage = "Ошибка базы данных";
        when(recipeService.findPublishedRecipesWithFilters(null, null, null))
                .thenThrow(new RuntimeException(errorMessage));
        when(messageProvider.getMessage("recipes.load_error")).thenReturn("Ошибка загрузки рецептов: ");

        // Act
        ResponseEntity<ApiResponse<List<RecipeDto>>> response = adminRecipeController.getPublishedRecipes(null, null, null);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().message()).contains(errorMessage);
        verify(recipeService).findPublishedRecipesWithFilters(null, null, null);
    }

    @Test
    @DisplayName("Удаление рецепта - успешное удаление")
    void deleteRecipe_Success() {
        // Arrange
        when(messageProvider.getMessage("recipe.deleted")).thenReturn("Рецепт успешно удален");

        // Act
        ResponseEntity<ApiResponse<Void>> response = adminRecipeController.deleteRecipe(EXISTING_RECIPE_ID);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().message()).isEqualTo("Рецепт успешно удален");
        verify(recipeService).deleteRecipe(EXISTING_RECIPE_ID);
    }

    @Test
    @DisplayName("Удаление рецепта - ошибка при удалении")
    void deleteRecipe_Exception() {
        // Arrange
        String errorMessage = "Ошибка базы данных";
        doThrow(new RuntimeException(errorMessage)).when(recipeService).deleteRecipe(EXISTING_RECIPE_ID);
        when(messageProvider.getMessage("recipe.delete_error")).thenReturn("Ошибка удаления рецепта: ");

        // Act
        ResponseEntity<ApiResponse<Void>> response = adminRecipeController.deleteRecipe(EXISTING_RECIPE_ID);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().message()).contains(errorMessage);
        verify(recipeService).deleteRecipe(EXISTING_RECIPE_ID);
    }

    @Test
    @DisplayName("Удаление рецепта - рецепт не найден")
    void deleteRecipe_RecipeNotFound() {
        // Arrange
        String errorMessage = "Рецепт не найден";
        doThrow(new RuntimeException(errorMessage)).when(recipeService).deleteRecipe(NON_EXISTING_RECIPE_ID);
        when(messageProvider.getMessage("recipe.delete_error")).thenReturn("Ошибка удаления рецепта: ");

        // Act
        ResponseEntity<ApiResponse<Void>> response = adminRecipeController.deleteRecipe(NON_EXISTING_RECIPE_ID);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().message()).contains(errorMessage);
        verify(recipeService).deleteRecipe(NON_EXISTING_RECIPE_ID);
    }

    @Test
    @DisplayName("Получение опубликованных рецептов - проверка структуры данных")
    void getPublishedRecipes_CheckDataStructure() {
        // Arrange
        RecipeDto borschRecipe = createBorschRecipe();
        List<RecipeDto> recipes = List.of(borschRecipe);
        when(recipeService.findPublishedRecipesWithFilters(null, null, null)).thenReturn(recipes);

        // Act
        ResponseEntity<ApiResponse<List<RecipeDto>>> response = adminRecipeController.getPublishedRecipes(null, null, null);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();

        RecipeDto resultRecipe = response.getBody().data().get(0);
        assertThat(resultRecipe.id()).isEqualTo(EXISTING_RECIPE_ID);
        assertThat(resultRecipe.title()).isEqualTo(BORSCH_TITLE);
        assertThat(resultRecipe.published()).isTrue();
        assertThat(resultRecipe.category()).isNotNull();
        assertThat(resultRecipe.category().name()).isEqualTo("Супы");
        assertThat(resultRecipe.author()).isNotNull();
        assertThat(resultRecipe.author().user().username()).isEqualTo("chef_ivan");
        assertThat(resultRecipe.inventoryItems()).hasSize(1);
        assertThat(resultRecipe.ingredients()).hasSize(3);
        assertThat(resultRecipe.commentCount()).isEqualTo(2);

        verify(recipeService).findPublishedRecipesWithFilters(null, null, null);
    }
}