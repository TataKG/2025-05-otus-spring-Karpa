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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты для RecipeServiceImpl")
class RecipeServiceImplTest {

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
        // Локальные переменные вместо полей класса
        User testUser = new User("testuser", "test@example.com", "password");
        testUser.setId(1L);

        testCategory = new Category("Test Category", "Test Description");
        testCategory.setId(1L);

        testAuthor = new Author(testUser, "Test bio");
        testAuthor.setId(1L);

        testInventory = new Inventory("Test Inventory", "Test Description");
        testInventory.setId(1L);

        testRecipe = new Recipe("Test Recipe", testCategory, testAuthor, "Test Description");
        testRecipe.setId(1L);
        testRecipe.setPublished(true);
        testRecipe.getIngredients().addAll(List.of("Ingredient 1", "Ingredient 2"));
        // Создаем DTO с правильной структурой
        var testCategoryDto = new CategoryDto(1L, "Test Category", "Test Description", LocalDateTime.now());
        var testAuthorDto = new AuthorDto(1L,
                new UserDto(1L, "testuser", "test@example.com", true,
                        java.util.Set.of("USER"), LocalDateTime.now(), true),
                "Test bio", LocalDateTime.now(), 5, List.of("USER"));
        var testInventoryDto = new InventoryDto(1L, "Test Inventory", "Test Description", LocalDateTime.now());

        testRecipeDto = new RecipeDto(1L, "Test Recipe", testCategoryDto, testAuthorDto,
                List.of(testInventoryDto), List.of("Ingredient 1", "Ingredient 2"),
                "Test Description", 3, true, LocalDateTime.now(), LocalDateTime.now());

        testRecipeSummaryDto = new RecipeSummaryDto(1L, "Test Recipe", "Test Category",
                "testuser", 3, true, LocalDateTime.now());
    }

    @Test
    @DisplayName("Получение рецепта с деталями - успешно")
    void getRecipeWithDetails_ShouldReturnRecipe_WhenRecipeExists() {
        // Given
        Long recipeId = 1L;
        List<Comment> comments = List.of();

        when(recipeRepository.findByIdWithAllRelations(recipeId)).thenReturn(Optional.of(testRecipe));
        when(commentRepository.findByRecipeIdWithUser(recipeId)).thenReturn(comments);
        when(recipeConverter.toDto(testRecipe)).thenReturn(testRecipeDto);

        // When
        RecipeDto result = recipeService.getRecipeWithDetails(recipeId);

        // Then
        assertNotNull(result);
        assertEquals(testRecipeDto, result);
    }

    @Test
    @DisplayName("Получение рецепта с деталями - рецепт не найден")
    void getRecipeWithDetails_ShouldThrowException_WhenRecipeNotFound() {
        // Given
        Long recipeId = 1L;
        when(recipeRepository.findByIdWithAllRelations(recipeId)).thenReturn(Optional.empty());
        when(messageProvider.getMessage("recipe.not_found", recipeId)).thenReturn("Recipe not found");

        // When & Then
        assertThrows(EntityNotFoundException.class,
                () -> recipeService.getRecipeWithDetails(recipeId));
    }

    @Test
    @DisplayName("Создание рецепта - успешное создание")
    void createRecipe_ShouldCreateRecipe_WhenValidData() {
        // Given
        String title = "Test Recipe";
        Long categoryId = 1L;
        Long authorId = 1L;
        List<String> ingredients = List.of("Ingredient 1", "Ingredient 2");
        String description = "Test Description";
        boolean published = true;

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
        when(authorRepository.findById(authorId)).thenReturn(Optional.of(testAuthor));
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);
        when(recipeConverter.toDto(testRecipe)).thenReturn(testRecipeDto);

        // When
        RecipeDto result = recipeService.createRecipe(title, categoryId, authorId, ingredients, description, published);

        // Then
        assertNotNull(result);
        assertEquals(testRecipeDto, result);
        verify(recipeRepository).save(any(Recipe.class));
    }

    @Test
    @DisplayName("Создание рецепта с инвентарем - успешное создание")
    void createRecipeWithInventory_ShouldCreateRecipeWithInventory_WhenValidData() {
        // Given
        String title = "Test Recipe";
        Long categoryId = 1L;
        Long authorId = 1L;
        List<String> ingredients = List.of("Ingredient 1", "Ingredient 2");
        String description = "Test Description";
        List<Long> inventoryIds = List.of(1L);
        boolean published = true;

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
        when(authorRepository.findById(authorId)).thenReturn(Optional.of(testAuthor));
        when(inventoryRepository.findAllById(inventoryIds)).thenReturn(List.of(testInventory));
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);
        when(recipeConverter.toDto(testRecipe)).thenReturn(testRecipeDto);

        // When
        RecipeDto result = recipeService.createRecipeWithInventory(title, categoryId, authorId,
                ingredients, description, inventoryIds, published);

        // Then
        assertNotNull(result);
        assertEquals(testRecipeDto, result);
        verify(recipeRepository).save(any(Recipe.class));
        verify(inventoryRepository).findAllById(inventoryIds);
    }

    @Test
    @DisplayName("Создание рецепта - категория не найдена")
    void createRecipe_ShouldThrowException_WhenCategoryNotFound() {
        // Given
        String title = "Test Recipe";
        Long categoryId = 1L;
        Long authorId = 1L;
        List<String> ingredients = List.of("Ingredient 1", "Ingredient 2");
        String description = "Test Description";
        boolean published = true;

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());
        when(messageProvider.getMessage("category.not_found", categoryId)).thenReturn("Category not found");

        // When & Then
        assertThrows(EntityNotFoundException.class,
                () -> recipeService.createRecipe(title, categoryId, authorId, ingredients, description, published));

        verify(recipeRepository, never()).save(any(Recipe.class));
    }

    @Test
    @DisplayName("Создание рецепта - автор не найден")
    void createRecipe_ShouldThrowException_WhenAuthorNotFound() {
        // Given
        String title = "Test Recipe";
        Long categoryId = 1L;
        Long authorId = 1L;
        List<String> ingredients = List.of("Ingredient 1", "Ingredient 2");
        String description = "Test Description";
        boolean published = true;

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
        when(authorRepository.findById(authorId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class,
                () -> recipeService.createRecipe(title, categoryId, authorId, ingredients, description, published));

        verify(recipeRepository, never()).save(any(Recipe.class));
    }

    @Test
    @DisplayName("Создание рецепта - пустые ингредиенты")
    void createRecipe_ShouldThrowException_WhenNoValidIngredients() {
        // Given
        String title = "Test Recipe";
        Long categoryId = 1L;
        Long authorId = 1L;
        List<String> ingredients = List.of("", "   ");
        String description = "Test Description";
        boolean published = true;

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> recipeService.createRecipe(title, categoryId, authorId, ingredients, description, published));

        verify(recipeRepository, never()).save(any(Recipe.class));
    }

    @Test
    @DisplayName("Обновление рецепта - успешно")
    void updateRecipe_ShouldUpdateRecipe_WhenValidData() {
        // Given
        Long recipeId = 1L;
        String title = "Updated Recipe";
        Long categoryId = 1L;
        Long authorId = 1L;
        List<String> ingredients = List.of("Updated Ingredient 1", "Updated Ingredient 2");
        String description = "Updated Description";
        List<Long> inventoryIds = List.of(1L);
        boolean published = true;

        Category updatedCategory = new Category("Updated Category", "Updated Description");
        updatedCategory.setId(2L);

        when(recipeRepository.findByIdWithBasicRelations(recipeId)).thenReturn(Optional.of(testRecipe));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(updatedCategory));
        when(inventoryRepository.findAllById(inventoryIds)).thenReturn(List.of(testInventory));
        when(recipeRepository.save(testRecipe)).thenReturn(testRecipe);
        when(recipeConverter.toDto(testRecipe)).thenReturn(testRecipeDto);

        // When
        RecipeDto result = recipeService.updateRecipe(recipeId, title, categoryId, authorId,
                ingredients, description, inventoryIds, published);

        // Then
        assertNotNull(result);
        verify(recipeRepository).save(testRecipe);
        verify(recipeRepository).deleteIngredients(recipeId);
        verify(inventoryRepository).findAllById(inventoryIds);
    }

    @Test
    @DisplayName("Получение всех опубликованных рецептов - успешно")
    void getAllPublishedRecipes_ShouldReturnPublishedRecipes() {
        // Given
        List<Recipe> recipes = List.of(testRecipe);
        when(recipeRepository.findPublishedRecipesWithBasicAssociations()).thenReturn(recipes);
        when(recipeConverter.toSummaryDto(testRecipe)).thenReturn(testRecipeSummaryDto);

        // When
        List<RecipeSummaryDto> result = recipeService.getAllPublishedRecipes();

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(testRecipeSummaryDto, result.get(0));
    }

    @Test
    @DisplayName("Поиск опубликованных рецептов по названию - успешно")
    void searchPublishedRecipesByTitle_ShouldReturnMatchingRecipes() {
        // Given
        String searchTitle = "Test";
        List<Recipe> recipes = List.of(testRecipe);
        when(recipeRepository.findPublishedByTitleContainingIgnoreCase(searchTitle)).thenReturn(recipes);
        when(recipeConverter.toSummaryDto(testRecipe)).thenReturn(testRecipeSummaryDto);

        // When
        List<RecipeSummaryDto> result = recipeService.searchPublishedRecipesByTitle(searchTitle);

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(testRecipeSummaryDto, result.get(0));
    }

    @Test
    @DisplayName("Поиск опубликованных рецептов по ингредиенту - успешно")
    void searchPublishedRecipesByIngredient_ShouldReturnMatchingRecipes() {
        // Given
        String searchIngredient = "Ingredient";
        List<Recipe> recipes = List.of(testRecipe);
        when(recipeRepository.findPublishedByIngredientContaining(searchIngredient)).thenReturn(recipes);
        when(recipeConverter.toSummaryDto(testRecipe)).thenReturn(testRecipeSummaryDto);

        // When
        List<RecipeSummaryDto> result = recipeService.searchPublishedRecipesByIngredient(searchIngredient);

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(testRecipeSummaryDto, result.get(0));
    }

    @Test
    @DisplayName("Публикация рецепта - успешно")
    void publishRecipe_ShouldPublishRecipe_WhenRecipeExists() {
        // Given
        Long recipeId = 1L;
        testRecipe.setPublished(false);

        when(recipeRepository.findByIdWithBasicRelations(recipeId)).thenReturn(Optional.of(testRecipe));
        when(recipeRepository.save(testRecipe)).thenReturn(testRecipe);
        when(recipeConverter.toDto(testRecipe)).thenReturn(testRecipeDto);

        // When
        RecipeDto result = recipeService.publishRecipe(recipeId);

        // Then
        assertNotNull(result);
        assertTrue(testRecipe.isPublished());
        verify(recipeRepository).save(testRecipe);
    }

    @Test
    @DisplayName("Снятие с публикации рецепта - успешно")
    void unpublishRecipe_ShouldUnpublishRecipe_WhenRecipeExists() {
        // Given
        Long recipeId = 1L;
        testRecipe.setPublished(true);

        when(recipeRepository.findByIdWithBasicRelations(recipeId)).thenReturn(Optional.of(testRecipe));
        when(recipeRepository.save(testRecipe)).thenReturn(testRecipe);
        when(recipeConverter.toDto(testRecipe)).thenReturn(testRecipeDto);

        // When
        RecipeDto result = recipeService.unpublishRecipe(recipeId);

        // Then
        assertNotNull(result);
        assertFalse(testRecipe.isPublished());
        verify(recipeRepository).save(testRecipe);
    }

    @Test
    @DisplayName("Удаление рецепта - успешно")
    void deleteRecipe_ShouldDeleteRecipe_WhenRecipeExists() {
        // Given
        Long recipeId = 1L;
        when(recipeRepository.existsById(recipeId)).thenReturn(true);

        // When
        recipeService.deleteRecipe(recipeId);

        // Then
        verify(commentRepository).deleteByRecipeId(recipeId);
        verify(recipeRepository).deleteInventoryAssociations(recipeId);
        verify(recipeRepository).deleteIngredients(recipeId);
        verify(recipeRepository).deleteById(recipeId);
    }

    @Test
    @DisplayName("Удаление рецепта - рецепт не найден")
    void deleteRecipe_ShouldThrowException_WhenRecipeNotFound() {
        // Given
        Long recipeId = 1L;
        when(recipeRepository.existsById(recipeId)).thenReturn(false);
        when(messageProvider.getMessage("recipe.not_found", recipeId)).thenReturn("Recipe not found");

        // When & Then
        assertThrows(EntityNotFoundException.class,
                () -> recipeService.deleteRecipe(recipeId));

        verify(commentRepository, never()).deleteByRecipeId(anyLong());
        verify(recipeRepository, never()).deleteInventoryAssociations(anyLong());
        verify(recipeRepository, never()).deleteIngredients(anyLong());
        verify(recipeRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Получение рецептов по автору - успешно")
    void getRecipesByAuthor_ShouldReturnAuthorRecipes() {
        // Given
        Long authorId = 1L;
        List<Recipe> recipes = List.of(testRecipe);
        when(recipeRepository.findByAuthorIdWithDetails(authorId)).thenReturn(recipes);
        when(recipeConverter.toSummaryDto(testRecipe)).thenReturn(testRecipeSummaryDto);

        // When
        List<RecipeSummaryDto> result = recipeService.getRecipesByAuthor(authorId);

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(testRecipeSummaryDto, result.get(0));
    }

    @Test
    @DisplayName("Получение опубликованных рецептов по автору - успешно")
    void getPublishedRecipesByAuthor_ShouldReturnPublishedAuthorRecipes() {
        // Given
        Long authorId = 1L;
        List<Recipe> recipes = List.of(testRecipe);
        when(recipeRepository.findPublishedByAuthorIdWithDetails(authorId)).thenReturn(recipes);
        when(recipeConverter.toSummaryDto(testRecipe)).thenReturn(testRecipeSummaryDto);

        // When
        List<RecipeSummaryDto> result = recipeService.getPublishedRecipesByAuthor(authorId);

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(testRecipeSummaryDto, result.get(0));
    }

    @Test
    @DisplayName("Поиск опубликованных рецептов по фильтрам - успешно")
    void findPublishedRecipesByFilters_ShouldReturnFilteredRecipes() {
        // Given
        String title = "Test";
        Long categoryId = 1L;
        Long authorId = 1L;
        List<Recipe> recipes = List.of(testRecipe);
        when(recipeRepository.findPublishedByFilters(title, categoryId, authorId)).thenReturn(recipes);
        when(recipeConverter.toSummaryDto(testRecipe)).thenReturn(testRecipeSummaryDto);

        // When
        List<RecipeSummaryDto> result = recipeService.findPublishedRecipesByFilters(title, categoryId, authorId);

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(testRecipeSummaryDto, result.get(0));
    }

    @Test
    @DisplayName("Поиск опубликованных рецептов с фильтрами - по поисковому запросу")
    void findPublishedRecipesWithFilters_ShouldReturnRecipesBySearch() {
        // Given
        String search = "Test";
        List<Recipe> recipes = List.of(testRecipe);
        when(recipeRepository.findPublishedByTitleContainingIgnoreCase(search)).thenReturn(recipes);
        when(recipeConverter.toDto(testRecipe)).thenReturn(testRecipeDto);

        // When
        List<RecipeDto> result = recipeService.findPublishedRecipesWithFilters(search, null, null);

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(testRecipeDto, result.get(0));
    }

    @Test
    @DisplayName("Поиск опубликованных рецептов с фильтрами - по категории и автору")
    void findPublishedRecipesWithFilters_ShouldReturnRecipesByCategoryAndAuthor() {
        // Given
        Long categoryId = 1L;
        Long authorId = 1L;
        List<Recipe> recipes = List.of(testRecipe);
        when(recipeRepository.findPublishedByFilters(null, categoryId, authorId)).thenReturn(recipes);
        when(recipeConverter.toDto(testRecipe)).thenReturn(testRecipeDto);

        // When
        List<RecipeDto> result = recipeService.findPublishedRecipesWithFilters(null, categoryId, authorId);

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(testRecipeDto, result.get(0));
    }

    @Test
    @DisplayName("Получение недавних опубликованных рецептов - успешно")
    void getRecentPublishedRecipes_ShouldReturnRecentRecipes() {
        // Given
        int limit = 5;
        List<Recipe> recipes = List.of(testRecipe);
        when(recipeRepository.findRecentPublishedRecipes(limit)).thenReturn(recipes);
        when(recipeConverter.toSummaryDto(testRecipe)).thenReturn(testRecipeSummaryDto);

        // When
        List<RecipeSummaryDto> result = recipeService.getRecentPublishedRecipes(limit);

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(testRecipeSummaryDto, result.get(0));
    }

    @Test
    @DisplayName("Получение популярных опубликованных рецептов - успешно")
    void getPopularPublishedRecipes_ShouldReturnPopularRecipes() {
        // Given
        int limit = 5;
        List<Recipe> recipes = List.of(testRecipe);
        when(recipeRepository.findPopularPublishedRecipes(limit)).thenReturn(recipes);
        when(recipeConverter.toSummaryDto(testRecipe)).thenReturn(testRecipeSummaryDto);

        // When
        List<RecipeSummaryDto> result = recipeService.getPopularPublishedRecipes(limit);

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(testRecipeSummaryDto, result.get(0));
    }

    @Test
    @DisplayName("Получение количества опубликованных рецептов - успешно")
    void getPublishedRecipesCount_ShouldReturnCount() {
        // Given
        long expectedCount = 10L;
        when(recipeRepository.countByPublishedTrue()).thenReturn(expectedCount);

        // When
        long result = recipeService.getPublishedRecipesCount();

        // Then
        assertEquals(expectedCount, result);
    }

    @Test
    @DisplayName("Получение рецепта по ID - успешно")
    void getRecipeById_ShouldReturnRecipe_WhenRecipeExists() {
        // Given
        Long recipeId = 1L;
        when(recipeRepository.findByIdWithBasicRelations(recipeId)).thenReturn(Optional.of(testRecipe));
        when(recipeConverter.toDto(testRecipe)).thenReturn(testRecipeDto);

        // When
        Optional<RecipeDto> result = recipeService.getRecipeById(recipeId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testRecipeDto, result.get());
    }

    @Test
    @DisplayName("Получение рецепта по ID с базовыми отношениями - успешно")
    void getRecipeByIdWithBasicRelations_ShouldReturnRecipe_WhenRecipeExists() {
        // Given
        Long recipeId = 1L;
        when(recipeRepository.findByIdWithBasicRelations(recipeId)).thenReturn(Optional.of(testRecipe));
        when(recipeConverter.toDto(testRecipe)).thenReturn(testRecipeDto);

        // When
        Optional<RecipeDto> result = recipeService.getRecipeByIdWithBasicRelations(recipeId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testRecipeDto, result.get());
    }

    @Test
    @DisplayName("Получение рецепта по ID со всеми отношениями - успешно")
    void getRecipeByIdWithAllRelations_ShouldReturnRecipe_WhenRecipeExists() {
        // Given
        Long recipeId = 1L;
        when(recipeRepository.findByIdWithAllRelations(recipeId)).thenReturn(Optional.of(testRecipe));
        when(recipeConverter.toDto(testRecipe)).thenReturn(testRecipeDto);

        // When
        Optional<RecipeDto> result = recipeService.getRecipeByIdWithAllRelations(recipeId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testRecipeDto, result.get());
    }
}