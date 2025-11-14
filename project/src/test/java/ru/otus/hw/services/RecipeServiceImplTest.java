package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.converters.RecipeConverter;
import ru.otus.hw.dto.*;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.*;
import ru.otus.hw.repositories.*;
import ru.otus.hw.util.MessageProvider;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты для RecipeServiceImpl")
class RecipeServiceImplTest {

    private static final Long EXISTING_RECIPE_ID = 1L;
    private static final Long NON_EXISTING_RECIPE_ID = 999L;
    private static final Long EXISTING_CATEGORY_ID = 1L;
    private static final Long NON_EXISTING_CATEGORY_ID = 999L;
    private static final Long EXISTING_AUTHOR_ID = 1L;
    private static final Long NON_EXISTING_AUTHOR_ID = 999L;
    private static final Long EXISTING_INVENTORY_ID = 1L;

    private static final String BORSCH_TITLE = "Борщ украинский";
    private static final String UPDATED_TITLE = "Обновленный борщ";
    private static final String SEARCH_QUERY = "борщ";

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private RecipeConverter recipeConverter;

    @Mock
    private MessageProvider messageProvider;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private RecipeServiceImpl recipeService;

    private Category testCategory;
    private Author testAuthor;
    private Recipe testRecipe;
    private RecipeDto testRecipeDto;
    private RecipeSummaryDto testRecipeSummaryDto;
    private Inventory testInventory;

    @BeforeEach
    void setUp() {
        User testUser = new User("chef_ivan", "ivan@example.com", "password");
        testUser.setId(EXISTING_AUTHOR_ID);

        testCategory = new Category("Супы", "Первые блюда");
        testCategory.setId(EXISTING_CATEGORY_ID);

        testAuthor = new Author(testUser, "Профессиональный шеф-повар");
        testAuthor.setId(EXISTING_AUTHOR_ID);

        testInventory = new Inventory("Блендер", "Кухонный блендер");
        testInventory.setId(EXISTING_INVENTORY_ID);

        testRecipe = new Recipe(BORSCH_TITLE, testCategory, testAuthor, "Классический украинский борщ");
        testRecipe.setId(EXISTING_RECIPE_ID);
        testRecipe.setPublished(true);
        testRecipe.getIngredients().addAll(List.of("Говядина - 500г", "Свекла - 2шт", "Капуста - 300г"));

        CategoryDto testCategoryDto = new CategoryDto(EXISTING_CATEGORY_ID, "Супы", "Первые блюда", LocalDateTime.now());
        UserDto testUserDto = new UserDto(EXISTING_AUTHOR_ID, "chef_ivan", "ivan@example.com", true,
                Set.of("USER"), LocalDateTime.now(), true);
        AuthorDto testAuthorDto = new AuthorDto(EXISTING_AUTHOR_ID, testUserDto,
                "Профессиональный шеф-повар", LocalDateTime.now(), 5, List.of("USER"));
        InventoryDto testInventoryDto = new InventoryDto(EXISTING_INVENTORY_ID, "Блендер", "Кухонный блендер", LocalDateTime.now());

        testRecipeDto = new RecipeDto(EXISTING_RECIPE_ID, BORSCH_TITLE, testCategoryDto, testAuthorDto,
                List.of(testInventoryDto), List.of("Говядина - 500г", "Свекла - 2шт", "Капуста - 300г"),
                "Классический украинский борщ", 3, true, LocalDateTime.now(), LocalDateTime.now());

        testRecipeSummaryDto = new RecipeSummaryDto(EXISTING_RECIPE_ID, BORSCH_TITLE, "Супы",
                "chef_ivan", 3, true, LocalDateTime.now());
    }

    @Test
    @DisplayName("Получение рецепта с деталями - успешно")
    void getRecipeWithDetails_ShouldReturnRecipe_WhenRecipeExists() {
        // Arrange
        List<Comment> comments = List.of();

        when(recipeRepository.findByIdWithAllRelations(EXISTING_RECIPE_ID)).thenReturn(Optional.of(testRecipe));
        when(commentRepository.findByRecipeIdWithUser(EXISTING_RECIPE_ID)).thenReturn(comments);
        when(recipeConverter.toDto(testRecipe)).thenReturn(testRecipeDto);

        // Act
        RecipeDto result = recipeService.getRecipeWithDetails(EXISTING_RECIPE_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(testRecipeDto);
    }

    @Test
    @DisplayName("Получение рецепта с деталями - рецепт не найден")
    void getRecipeWithDetails_ShouldThrowException_WhenRecipeNotFound() {
        // Arrange
        when(recipeRepository.findByIdWithAllRelations(NON_EXISTING_RECIPE_ID)).thenReturn(Optional.empty());
        when(messageProvider.getMessage("recipe.not_found", NON_EXISTING_RECIPE_ID))
                .thenReturn("Рецепт не найден: " + NON_EXISTING_RECIPE_ID);

        // Act & Assert
        assertThatThrownBy(() -> recipeService.getRecipeWithDetails(NON_EXISTING_RECIPE_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Рецепт не найден: " + NON_EXISTING_RECIPE_ID);
    }

    @Test
    @DisplayName("Создание рецепта - успешное создание")
    void createRecipe_ShouldCreateRecipe_WhenValidData() {
        // Arrange
        String title = "Новый рецепт пасты";
        List<String> ingredients = List.of("Спагетти - 200г", "Помидоры - 2шт", "Чеснок - 2 зубчика");
        String description = "Простой рецепт пасты";
        boolean published = true;

        when(categoryRepository.findById(EXISTING_CATEGORY_ID)).thenReturn(Optional.of(testCategory));
        when(authorRepository.findById(EXISTING_AUTHOR_ID)).thenReturn(Optional.of(testAuthor));
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);
        when(recipeConverter.toDto(testRecipe)).thenReturn(testRecipeDto);

        // Act
        RecipeDto result = recipeService.createRecipe(title, EXISTING_CATEGORY_ID, EXISTING_AUTHOR_ID,
                ingredients, description, published);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(testRecipeDto);
        verify(recipeRepository).save(any(Recipe.class));
    }

    @Test
    @DisplayName("Создание рецепта с инвентарем - успешное создание")
    void createRecipeWithInventory_ShouldCreateRecipeWithInventory_WhenValidData() {
        // Arrange
        String title = "Новый рецепт пасты";
        List<String> ingredients = List.of("Спагетти - 200г", "Помидоры - 2шт", "Чеснок - 2 зубчика");
        String description = "Простой рецепт пасты";
        List<Long> inventoryIds = List.of(EXISTING_INVENTORY_ID);
        boolean published = true;

        when(categoryRepository.findById(EXISTING_CATEGORY_ID)).thenReturn(Optional.of(testCategory));
        when(authorRepository.findById(EXISTING_AUTHOR_ID)).thenReturn(Optional.of(testAuthor));
        when(inventoryRepository.findAllById(inventoryIds)).thenReturn(List.of(testInventory));
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);
        when(recipeConverter.toDto(testRecipe)).thenReturn(testRecipeDto);

        // Act
        RecipeDto result = recipeService.createRecipeWithInventory(title, EXISTING_CATEGORY_ID, EXISTING_AUTHOR_ID,
                ingredients, description, inventoryIds, published);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(testRecipeDto);
        verify(recipeRepository).save(any(Recipe.class));
        verify(inventoryRepository).findAllById(inventoryIds);
    }

    @Test
    @DisplayName("Создание рецепта - категория не найдена")
    void createRecipe_ShouldThrowException_WhenCategoryNotFound() {
        // Arrange
        String title = "Новый рецепт";
        List<String> ingredients = List.of("Ингредиент 1", "Ингредиент 2");
        String description = "Описание рецепта";
        boolean published = true;

        when(categoryRepository.findById(NON_EXISTING_CATEGORY_ID)).thenReturn(Optional.empty());
        when(messageProvider.getMessage("category.not_found", NON_EXISTING_CATEGORY_ID))
                .thenReturn("Категория не найдена: " + NON_EXISTING_CATEGORY_ID);

        // Act & Assert
        assertThatThrownBy(() -> recipeService.createRecipe(title, NON_EXISTING_CATEGORY_ID, EXISTING_AUTHOR_ID,
                ingredients, description, published))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Категория не найдена: " + NON_EXISTING_CATEGORY_ID);

        verify(recipeRepository, never()).save(any(Recipe.class));
    }

    @Test
    @DisplayName("Создание рецепта - автор не найден")
    void createRecipe_ShouldThrowException_WhenAuthorNotFound() {
        // Arrange
        String title = "Новый рецепт";
        List<String> ingredients = List.of("Ингредиент 1", "Ингредиент 2");
        String description = "Описание рецепта";
        boolean published = true;

        when(categoryRepository.findById(EXISTING_CATEGORY_ID)).thenReturn(Optional.of(testCategory));
        when(authorRepository.findById(NON_EXISTING_AUTHOR_ID)).thenReturn(Optional.empty());
        when(messageProvider.getMessage("author.not_found", NON_EXISTING_AUTHOR_ID))
                .thenReturn("Автор не найден: " + NON_EXISTING_AUTHOR_ID);

        // Act & Assert
        assertThatThrownBy(() -> recipeService.createRecipe(title, EXISTING_CATEGORY_ID, NON_EXISTING_AUTHOR_ID,
                ingredients, description, published))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Автор не найден: " + NON_EXISTING_AUTHOR_ID);

        verify(recipeRepository, never()).save(any(Recipe.class));
    }

    @Test
    @DisplayName("Обновление рецепта - успешно")
    void updateRecipe_ShouldUpdateRecipe_WhenValidData() {
        // Arrange
        List<String> ingredients = List.of("Обновленный ингредиент 1", "Обновленный ингредиент 2");
        String description = "Обновленное описание";
        List<Long> inventoryIds = List.of(EXISTING_INVENTORY_ID);
        boolean published = true;

        when(recipeRepository.findByIdWithBasicRelations(EXISTING_RECIPE_ID)).thenReturn(Optional.of(testRecipe));
        when(categoryRepository.findById(EXISTING_CATEGORY_ID)).thenReturn(Optional.of(testCategory));
        when(inventoryRepository.findAllById(inventoryIds)).thenReturn(List.of(testInventory));
        when(recipeRepository.save(testRecipe)).thenReturn(testRecipe);
        when(recipeConverter.toDto(testRecipe)).thenReturn(testRecipeDto);

        // Act
        RecipeDto result = recipeService.updateRecipe(EXISTING_RECIPE_ID, UPDATED_TITLE, EXISTING_CATEGORY_ID, EXISTING_AUTHOR_ID,
                ingredients, description, inventoryIds, published);

        // Assert
        assertThat(result).isNotNull();
        verify(recipeRepository).save(testRecipe);
        verify(recipeRepository).deleteIngredients(EXISTING_RECIPE_ID);
        verify(inventoryRepository).findAllById(inventoryIds);
    }

    @Test
    @DisplayName("Обновление рецепта - пустые ингредиенты (бизнес-валидация)")
    void updateRecipe_ShouldThrowException_WhenNoValidIngredients() {
        // Arrange
        List<String> ingredients = List.of("", "   ");
        String description = "Обновленное описание";
        List<Long> inventoryIds = List.of(EXISTING_INVENTORY_ID);
        boolean published = true;

        when(messageProvider.getMessage("recipe.ingredients.min.one"))
                .thenReturn("Добавьте хотя бы один непустой ингредиент");

        // Act & Assert
        assertThatThrownBy(() -> recipeService.updateRecipe(EXISTING_RECIPE_ID, UPDATED_TITLE, EXISTING_CATEGORY_ID, EXISTING_AUTHOR_ID,
                ingredients, description, inventoryIds, published))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Добавьте хотя бы один непустой ингредиент");

        verify(recipeRepository, never()).save(any(Recipe.class));
    }

    @Test
    @DisplayName("Обновление рецепта - null ингредиенты (бизнес-валидация)")
    void updateRecipe_ShouldThrowException_WhenIngredientsNull() {
        // Arrange
        String description = "Обновленное описание";

        when(messageProvider.getMessage("recipe.ingredients.min.one"))
                .thenReturn("Добавьте хотя бы один непустой ингредиент");

        // Act & Assert
        assertThatThrownBy(() -> recipeService.updateRecipe(EXISTING_RECIPE_ID, UPDATED_TITLE, EXISTING_CATEGORY_ID, EXISTING_AUTHOR_ID,
                null, description, List.of(EXISTING_INVENTORY_ID), true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Добавьте хотя бы один непустой ингредиент");

        verify(recipeRepository, never()).save(any(Recipe.class));
    }

    @Test
    @DisplayName("Обновление рецепта - пустой список ингредиентов (бизнес-валидация)")
    void updateRecipe_ShouldThrowException_WhenIngredientsEmpty() {
        // Arrange
        List<String> ingredients = List.of();
        String description = "Обновленное описание";
        List<Long> inventoryIds = List.of(EXISTING_INVENTORY_ID);
        boolean published = true;

        when(messageProvider.getMessage("recipe.ingredients.min.one"))
                .thenReturn("Добавьте хотя бы один непустой ингредиент");

        // Act & Assert
        assertThatThrownBy(() -> recipeService.updateRecipe(EXISTING_RECIPE_ID, UPDATED_TITLE, EXISTING_CATEGORY_ID, EXISTING_AUTHOR_ID,
                ingredients, description, inventoryIds, published))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Добавьте хотя бы один непустой ингредиент");

        verify(recipeRepository, never()).save(any(Recipe.class));
    }

    @Test
    @DisplayName("Обновление рецепта - рецепт не найден")
    void updateRecipe_ShouldThrowException_WhenRecipeNotFound() {
        // Arrange
        List<String> ingredients = List.of("Ингредиент 1", "Ингредиент 2");
        String description = "Описание рецепта";
        List<Long> inventoryIds = List.of(EXISTING_INVENTORY_ID);
        boolean published = true;

        when(recipeRepository.findByIdWithBasicRelations(NON_EXISTING_RECIPE_ID)).thenReturn(Optional.empty());
        when(messageProvider.getMessage("recipe.not_found", NON_EXISTING_RECIPE_ID))
                .thenReturn("Рецепт не найден: " + NON_EXISTING_RECIPE_ID);

        // Act & Assert
        assertThatThrownBy(() -> recipeService.updateRecipe(NON_EXISTING_RECIPE_ID, UPDATED_TITLE, EXISTING_CATEGORY_ID,
                EXISTING_AUTHOR_ID, ingredients, description, inventoryIds, published))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Рецепт не найден: " + NON_EXISTING_RECIPE_ID);

        verify(recipeRepository, never()).save(any(Recipe.class));
    }

    @Test
    @DisplayName("Обновление рецепта - категория не найдена")
    void updateRecipe_ShouldThrowException_WhenCategoryNotFound() {
        // Arrange
        List<String> ingredients = List.of("Ингредиент 1", "Ингредиент 2");
        String description = "Описание рецепта";
        List<Long> inventoryIds = List.of(EXISTING_INVENTORY_ID);
        boolean published = true;

        when(recipeRepository.findByIdWithBasicRelations(EXISTING_RECIPE_ID)).thenReturn(Optional.of(testRecipe));
        when(categoryRepository.findById(NON_EXISTING_CATEGORY_ID)).thenReturn(Optional.empty());
        when(messageProvider.getMessage("category.not_found", NON_EXISTING_CATEGORY_ID))
                .thenReturn("Категория не найдена: " + NON_EXISTING_CATEGORY_ID);

        // Act & Assert
        assertThatThrownBy(() -> recipeService.updateRecipe(EXISTING_RECIPE_ID, UPDATED_TITLE, NON_EXISTING_CATEGORY_ID,
                EXISTING_AUTHOR_ID, ingredients, description, inventoryIds, published))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Категория не найдена: " + NON_EXISTING_CATEGORY_ID);

        verify(recipeRepository, never()).save(any(Recipe.class));
    }

    @Test
    @DisplayName("Получение всех опубликованных рецептов - успешно")
    void getAllPublishedRecipes_ShouldReturnPublishedRecipes() {
        // Arrange
        List<Recipe> recipes = List.of(testRecipe);
        when(recipeRepository.findPublishedRecipesWithBasicAssociations()).thenReturn(recipes);
        when(recipeConverter.toSummaryDto(testRecipe)).thenReturn(testRecipeSummaryDto);

        // Act
        List<RecipeSummaryDto> result = recipeService.getAllPublishedRecipes();

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testRecipeSummaryDto);
    }

    @Test
    @DisplayName("Поиск опубликованных рецептов по названию - успешно")
    void searchPublishedRecipesByTitle_ShouldReturnMatchingRecipes() {
        // Arrange
        List<Recipe> recipes = List.of(testRecipe);
        when(recipeRepository.findPublishedByTitleContainingIgnoreCase(SEARCH_QUERY)).thenReturn(recipes);
        when(recipeConverter.toSummaryDto(testRecipe)).thenReturn(testRecipeSummaryDto);

        // Act
        List<RecipeSummaryDto> result = recipeService.searchPublishedRecipesByTitle(SEARCH_QUERY);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testRecipeSummaryDto);
    }

    @Test
    @DisplayName("Поиск опубликованных рецептов по ингредиенту - успешно")
    void searchPublishedRecipesByIngredient_ShouldReturnMatchingRecipes() {
        // Arrange
        String searchIngredient = "говядина";
        List<Recipe> recipes = List.of(testRecipe);
        when(recipeRepository.findPublishedByIngredientContaining(searchIngredient)).thenReturn(recipes);
        when(recipeConverter.toSummaryDto(testRecipe)).thenReturn(testRecipeSummaryDto);

        // Act
        List<RecipeSummaryDto> result = recipeService.searchPublishedRecipesByIngredient(searchIngredient);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testRecipeSummaryDto);
    }

    @Test
    @DisplayName("Публикация рецепта - успешно")
    void publishRecipe_ShouldPublishRecipe_WhenRecipeExists() {
        // Arrange
        testRecipe.setPublished(false);

        when(recipeRepository.findByIdWithBasicRelations(EXISTING_RECIPE_ID)).thenReturn(Optional.of(testRecipe));
        when(recipeRepository.save(testRecipe)).thenReturn(testRecipe);
        when(recipeConverter.toDto(testRecipe)).thenReturn(testRecipeDto);

        // Act
        RecipeDto result = recipeService.publishRecipe(EXISTING_RECIPE_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(testRecipe.isPublished()).isTrue();
        verify(recipeRepository).save(testRecipe);
    }

    @Test
    @DisplayName("Снятие с публикации рецепта - успешно")
    void unpublishRecipe_ShouldUnpublishRecipe_WhenRecipeExists() {
        // Arrange
        testRecipe.setPublished(true);

        when(recipeRepository.findByIdWithBasicRelations(EXISTING_RECIPE_ID)).thenReturn(Optional.of(testRecipe));
        when(recipeRepository.save(testRecipe)).thenReturn(testRecipe);
        when(recipeConverter.toDto(testRecipe)).thenReturn(testRecipeDto);

        // Act
        RecipeDto result = recipeService.unpublishRecipe(EXISTING_RECIPE_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(testRecipe.isPublished()).isFalse();
        verify(recipeRepository).save(testRecipe);
    }

    @Test
    @DisplayName("Удаление рецепта - успешно")
    void deleteRecipe_ShouldDeleteRecipe_WhenRecipeExists() {
        // Arrange
        when(recipeRepository.existsById(EXISTING_RECIPE_ID)).thenReturn(true);

        // Act
        recipeService.deleteRecipe(EXISTING_RECIPE_ID);

        // Assert
        verify(commentRepository).deleteByRecipeId(EXISTING_RECIPE_ID);
        verify(recipeRepository).deleteInventoryAssociations(EXISTING_RECIPE_ID);
        verify(recipeRepository).deleteIngredients(EXISTING_RECIPE_ID);
        verify(recipeRepository).deleteById(EXISTING_RECIPE_ID);
    }

    @Test
    @DisplayName("Удаление рецепта - рецепт не найден")
    void deleteRecipe_ShouldThrowException_WhenRecipeNotFound() {
        // Arrange
        when(recipeRepository.existsById(NON_EXISTING_RECIPE_ID)).thenReturn(false);
        when(messageProvider.getMessage("recipe.not_found", NON_EXISTING_RECIPE_ID))
                .thenReturn("Рецепт не найден: " + NON_EXISTING_RECIPE_ID);

        // Act & Assert
        assertThatThrownBy(() -> recipeService.deleteRecipe(NON_EXISTING_RECIPE_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Рецепт не найден: " + NON_EXISTING_RECIPE_ID);

        verify(commentRepository, never()).deleteByRecipeId(anyLong());
        verify(recipeRepository, never()).deleteInventoryAssociations(anyLong());
        verify(recipeRepository, never()).deleteIngredients(anyLong());
        verify(recipeRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Получение рецептов по автору - успешно")
    void getRecipesByAuthor_ShouldReturnAuthorRecipes() {
        // Arrange
        List<Recipe> recipes = List.of(testRecipe);
        when(recipeRepository.findByAuthorIdWithDetails(EXISTING_AUTHOR_ID)).thenReturn(recipes);
        when(recipeConverter.toSummaryDto(testRecipe)).thenReturn(testRecipeSummaryDto);

        // Act
        List<RecipeSummaryDto> result = recipeService.getRecipesByAuthor(EXISTING_AUTHOR_ID);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testRecipeSummaryDto);
    }

    @Test
    @DisplayName("Получение опубликованных рецептов по автору - успешно")
    void getPublishedRecipesByAuthor_ShouldReturnPublishedAuthorRecipes() {
        // Arrange
        List<Recipe> recipes = List.of(testRecipe);
        when(recipeRepository.findPublishedByAuthorIdWithDetails(EXISTING_AUTHOR_ID)).thenReturn(recipes);
        when(recipeConverter.toSummaryDto(testRecipe)).thenReturn(testRecipeSummaryDto);

        // Act
        List<RecipeSummaryDto> result = recipeService.getPublishedRecipesByAuthor(EXISTING_AUTHOR_ID);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testRecipeSummaryDto);
    }

    @Test
    @DisplayName("Поиск опубликованных рецептов по фильтрам - успешно")
    void findPublishedRecipesByFilters_ShouldReturnFilteredRecipes() {
        // Arrange
        List<Recipe> recipes = List.of(testRecipe);
        when(recipeRepository.findPublishedByFilters(SEARCH_QUERY, EXISTING_CATEGORY_ID, EXISTING_AUTHOR_ID)).thenReturn(recipes);
        when(recipeConverter.toSummaryDto(testRecipe)).thenReturn(testRecipeSummaryDto);

        // Act
        List<RecipeSummaryDto> result = recipeService.findPublishedRecipesByFilters(SEARCH_QUERY, EXISTING_CATEGORY_ID, EXISTING_AUTHOR_ID);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testRecipeSummaryDto);
    }

    @Test
    @DisplayName("Поиск опубликованных рецептов с фильтрами - по поисковому запросу")
    void findPublishedRecipesWithFilters_ShouldReturnRecipesBySearch() {
        // Arrange
        List<Recipe> recipes = List.of(testRecipe);
        when(recipeRepository.findPublishedByTitleContainingIgnoreCase(SEARCH_QUERY)).thenReturn(recipes);
        when(recipeConverter.toDto(testRecipe)).thenReturn(testRecipeDto);

        // Act
        List<RecipeDto> result = recipeService.findPublishedRecipesWithFilters(SEARCH_QUERY, null, null);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testRecipeDto);
    }

    @Test
    @DisplayName("Поиск опубликованных рецептов с фильтрами - по категории и автору")
    void findPublishedRecipesWithFilters_ShouldReturnRecipesByCategoryAndAuthor() {
        // Arrange
        List<Recipe> recipes = List.of(testRecipe);
        when(recipeRepository.findPublishedByFilters(null, EXISTING_CATEGORY_ID, EXISTING_AUTHOR_ID)).thenReturn(recipes);
        when(recipeConverter.toDto(testRecipe)).thenReturn(testRecipeDto);

        // Act
        List<RecipeDto> result = recipeService.findPublishedRecipesWithFilters(null, EXISTING_CATEGORY_ID, EXISTING_AUTHOR_ID);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testRecipeDto);
    }

    @Test
    @DisplayName("Получение недавних опубликованных рецептов - успешно")
    void getRecentPublishedRecipes_ShouldReturnRecentRecipes() {
        // Arrange
        int limit = 5;
        List<Recipe> recipes = List.of(testRecipe);
        when(recipeRepository.findRecentPublishedRecipes(limit)).thenReturn(recipes);
        when(recipeConverter.toSummaryDto(testRecipe)).thenReturn(testRecipeSummaryDto);

        // Act
        List<RecipeSummaryDto> result = recipeService.getRecentPublishedRecipes(limit);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testRecipeSummaryDto);
    }

    @Test
    @DisplayName("Получение популярных опубликованных рецептов - успешно")
    void getPopularPublishedRecipes_ShouldReturnPopularRecipes() {
        // Arrange
        int limit = 5;
        List<Recipe> recipes = List.of(testRecipe);
        when(recipeRepository.findPopularPublishedRecipes(limit)).thenReturn(recipes);
        when(recipeConverter.toSummaryDto(testRecipe)).thenReturn(testRecipeSummaryDto);

        // Act
        List<RecipeSummaryDto> result = recipeService.getPopularPublishedRecipes(limit);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testRecipeSummaryDto);
    }

    @Test
    @DisplayName("Получение количества опубликованных рецептов - успешно")
    void getPublishedRecipesCount_ShouldReturnCount() {
        // Arrange
        long expectedCount = 10L;
        when(recipeRepository.countByPublishedTrue()).thenReturn(expectedCount);

        // Act
        long result = recipeService.getPublishedRecipesCount();

        // Assert
        assertThat(result).isEqualTo(expectedCount);
    }

    @Test
    @DisplayName("Получение рецепта по ID - успешно")
    void getRecipeById_ShouldReturnRecipe_WhenRecipeExists() {
        // Arrange
        when(recipeRepository.findByIdWithBasicRelations(EXISTING_RECIPE_ID)).thenReturn(Optional.of(testRecipe));
        when(recipeConverter.toDto(testRecipe)).thenReturn(testRecipeDto);

        // Act
        Optional<RecipeDto> result = recipeService.getRecipeById(EXISTING_RECIPE_ID);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testRecipeDto);
    }

    @Test
    @DisplayName("Получение рецепта по ID с базовыми отношениями - успешно")
    void getRecipeByIdWithBasicRelations_ShouldReturnRecipe_WhenRecipeExists() {
        // Arrange
        when(recipeRepository.findByIdWithBasicRelations(EXISTING_RECIPE_ID)).thenReturn(Optional.of(testRecipe));
        when(recipeConverter.toDto(testRecipe)).thenReturn(testRecipeDto);

        // Act
        Optional<RecipeDto> result = recipeService.getRecipeByIdWithBasicRelations(EXISTING_RECIPE_ID);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testRecipeDto);
    }

    @Test
    @DisplayName("Получение рецепта по ID со всеми отношениями - успешно")
    void getRecipeByIdWithAllRelations_ShouldReturnRecipe_WhenRecipeExists() {
        // Arrange
        when(recipeRepository.findByIdWithAllRelations(EXISTING_RECIPE_ID)).thenReturn(Optional.of(testRecipe));
        when(recipeConverter.toDto(testRecipe)).thenReturn(testRecipeDto);

        // Act
        Optional<RecipeDto> result = recipeService.getRecipeByIdWithAllRelations(EXISTING_RECIPE_ID);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testRecipeDto);
    }
}