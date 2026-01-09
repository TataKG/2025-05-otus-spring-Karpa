package ru.otus.hw.controllers.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.config.SecurityConfig;
import ru.otus.hw.dto.*;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.*;
import ru.otus.hw.utils.MessageProvider;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecipeController.class)
@Import(SecurityConfig.class)
class RecipeControllerTest {

    private static final Long RECIPE_ID = 1L;
    private static final Long AUTHOR_ID = 1L;
    private static final Long CATEGORY_ID = 1L;
    private static final Long NON_EXISTING_RECIPE_ID = 999L;
    private static final String USERNAME = "test_user";
    private static final String RECIPE_TITLE = "Test Recipe";
    private static final String RECIPE_DESCRIPTION = "Test recipe description";

    private static final String USER_NOT_AUTHENTICATED_MESSAGE = "Пользователь не аутентифицирован";
    private static final String AUTHOR_NOT_FOUND_MESSAGE = "Автор не найден";
    private static final String AUTHOR_NOT_FOUND_FOR_USER_MESSAGE = "Автор не найден для пользователя";
    private static final String RECIPE_FORM_DATA_LOAD_FAILED_MESSAGE = "Ошибка загрузки данных формы";
    private static final String RECIPE_EDIT_FORM_DATA_LOAD_FAILED_MESSAGE = "Ошибка загрузки данных формы редактирования";
    private static final String RECIPE_MY_RECIPES_LOAD_FAILED_MESSAGE = "Ошибка загрузки моих рецептов";
    private static final String RECIPE_CREATED_PUBLISHED_MESSAGE = "Рецепт создан и опубликован";
    private static final String RECIPE_CREATED_DRAFT_MESSAGE = "Рецепт создан как черновик";
    private static final String RECIPE_CREATE_FAILED_MESSAGE = "Ошибка создания рецепта";
    private static final String RECIPE_UPDATED_PUBLISHED_MESSAGE = "Рецепт обновлен и опубликован";
    private static final String RECIPE_UPDATED_DRAFT_MESSAGE = "Рецепт обновлен как черновик";
    private static final String RECIPE_UPDATE_FAILED_MESSAGE = "Ошибка обновления рецепта";
    private static final String RECIPE_NOT_FOUND_MESSAGE = "Рецепт не найден";
    private static final String RECIPE_NOT_FOUND_OR_UNPUBLISHED_MESSAGE = "Рецепт не найден или не опубликован";
    private static final String RECIPE_LOAD_FAILED_MESSAGE = "Ошибка загрузки рецептов";
    private static final String RECIPE_DELETED_MESSAGE = "Рецепт удален";
    private static final String RECIPE_ALREADY_DELETED_MESSAGE = "Рецепт уже удален";
    private static final String RECIPE_DELETE_FAILED_MESSAGE = "Ошибка удаления рецепта";
    private static final String RECIPE_DETAILS_LOAD_FAILED_MESSAGE = "Ошибка загрузки деталей рецепта";
    private static final String RECIPE_PUBLISHED_MESSAGE = "Рецепт опубликован";
    private static final String RECIPE_PUBLISH_FAILED_MESSAGE = "Ошибка публикации рецепта";
    private static final String RECIPE_UNPUBLISHED_MESSAGE = "Рецепт снят с публикации";
    private static final String RECIPE_UNPUBLISH_FAILED_MESSAGE = "Ошибка снятия с публикации";
    private static final String RECIPE_OPERATION_OWN_ONLY_MESSAGE = "Операция разрешена только для собственных рецептов";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecipeService recipeService;

    @MockBean
    private MessageProvider messageProvider;

    @MockBean
    private CommentService commentService;

    @MockBean
    private AuthorService authorService;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private InventoryService inventoryService;

    private Authentication authentication;
    private AuthorDto testAuthorDto;
    private RecipeDto testRecipeDto;
    private RecipeSummaryDto testRecipeSummaryDto;
    private CategoryDto testCategoryDto;
    private InventoryDto testInventoryDto;

    @BeforeEach
    void setUp() {
        authentication = new UsernamePasswordAuthenticationToken(
                USERNAME,
                null,
                List.of()
        );

        UserDto testUserDto = new UserDto(
                1L,
                USERNAME,
                "test@example.com",
                true,
                Set.of("ROLE_USER"),
                LocalDateTime.now(),
                true
        );

        testAuthorDto = new AuthorDto(
                AUTHOR_ID,
                testUserDto,
                "Test bio",
                LocalDateTime.now(),
                5,
                List.of("ROLE_USER")
        );

        testCategoryDto = new CategoryDto(
                CATEGORY_ID,
                "Test Category",
                "Test category description",
                LocalDateTime.now()
        );

        testInventoryDto = new InventoryDto(
                1L,
                "Test Inventory",
                "Test inventory description",
                LocalDateTime.now()
        );

        testRecipeDto = new RecipeDto(
                RECIPE_ID,
                RECIPE_TITLE,
                testCategoryDto,
                testAuthorDto,
                List.of(testInventoryDto),
                List.of("Ingredient 1", "Ingredient 2"),
                RECIPE_DESCRIPTION,
                3,
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        testRecipeSummaryDto = new RecipeSummaryDto(
                RECIPE_ID,
                RECIPE_TITLE,
                "Test Category",
                "Test Author",
                3,
                true,
                LocalDateTime.now()
        );

        // Mock message provider responses
        when(messageProvider.getMessage("user.not_authenticated")).thenReturn(USER_NOT_AUTHENTICATED_MESSAGE);
        when(messageProvider.getMessage("author.not_found")).thenReturn(AUTHOR_NOT_FOUND_MESSAGE);
        when(messageProvider.getMessage("author.not_found_for_user", USERNAME)).thenReturn(AUTHOR_NOT_FOUND_FOR_USER_MESSAGE);
        when(messageProvider.getMessage("recipe.form_data.load_failed")).thenReturn(RECIPE_FORM_DATA_LOAD_FAILED_MESSAGE);
        when(messageProvider.getMessage("recipe.edit.form_data.load_failed")).thenReturn(RECIPE_EDIT_FORM_DATA_LOAD_FAILED_MESSAGE);
        when(messageProvider.getMessage("recipe.my_recipes.load_failed")).thenReturn(RECIPE_MY_RECIPES_LOAD_FAILED_MESSAGE);
        when(messageProvider.getMessage("recipe.created.published")).thenReturn(RECIPE_CREATED_PUBLISHED_MESSAGE);
        when(messageProvider.getMessage("recipe.created.draft")).thenReturn(RECIPE_CREATED_DRAFT_MESSAGE);
        when(messageProvider.getMessage("recipe.create.failed")).thenReturn(RECIPE_CREATE_FAILED_MESSAGE);
        when(messageProvider.getMessage("recipe.updated.published")).thenReturn(RECIPE_UPDATED_PUBLISHED_MESSAGE);
        when(messageProvider.getMessage("recipe.updated.draft")).thenReturn(RECIPE_UPDATED_DRAFT_MESSAGE);
        when(messageProvider.getMessage("recipe.update.failed")).thenReturn(RECIPE_UPDATE_FAILED_MESSAGE);
        when(messageProvider.getMessage("recipe.not_found", RECIPE_ID)).thenReturn(RECIPE_NOT_FOUND_MESSAGE);
        when(messageProvider.getMessage("recipe.not_found_or_unpublished")).thenReturn(RECIPE_NOT_FOUND_OR_UNPUBLISHED_MESSAGE);
        when(messageProvider.getMessage("recipe.load_failed")).thenReturn(RECIPE_LOAD_FAILED_MESSAGE);
        when(messageProvider.getMessage("recipe.deleted")).thenReturn(RECIPE_DELETED_MESSAGE);
        when(messageProvider.getMessage("recipe.already_deleted")).thenReturn(RECIPE_ALREADY_DELETED_MESSAGE);
        when(messageProvider.getMessage("recipe.delete.failed")).thenReturn(RECIPE_DELETE_FAILED_MESSAGE);
        when(messageProvider.getMessage("recipe.details.load_failed")).thenReturn(RECIPE_DETAILS_LOAD_FAILED_MESSAGE);
        when(messageProvider.getMessage("recipe.published")).thenReturn(RECIPE_PUBLISHED_MESSAGE);
        when(messageProvider.getMessage("recipe.publish.failed")).thenReturn(RECIPE_PUBLISH_FAILED_MESSAGE);
        when(messageProvider.getMessage("recipe.unpublished")).thenReturn(RECIPE_UNPUBLISHED_MESSAGE);
        when(messageProvider.getMessage("recipe.unpublish.failed")).thenReturn(RECIPE_UNPUBLISH_FAILED_MESSAGE);
        when(messageProvider.getMessage("recipe.operation.own_only")).thenReturn(RECIPE_OPERATION_OWN_ONLY_MESSAGE);
    }

    @Test
    @DisplayName("Получение данных формы создания - успешный случай")
    void getCreateFormData_ShouldReturnFormData_WhenAuthenticatedWithAuthor() throws Exception {
        // Arrange
        when(authorService.getAuthorByUsername(USERNAME)).thenReturn(Optional.of(testAuthorDto));
        when(categoryService.getAllCategories()).thenReturn(List.of(testCategoryDto));
        when(inventoryService.getAllInventory()).thenReturn(List.of(testInventoryDto));

        // Act & Assert
        mockMvc.perform(get("/api/recipes/create-form-data")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.recipe").exists())
                .andExpect(jsonPath("$.data.categories").isArray())
                .andExpect(jsonPath("$.data.categories.length()").value(1))
                .andExpect(jsonPath("$.data.inventoryItems").isArray())
                .andExpect(jsonPath("$.data.inventoryItems.length()").value(1));
    }

    @Test
    @DisplayName("Получение данных формы создания - не аутентифицированный пользователь")
    void getCreateFormData_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/recipes/create-form-data")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(USER_NOT_AUTHENTICATED_MESSAGE));
    }

    @Test
    @DisplayName("Получение данных формы создания - автор не найден")
    void getCreateFormData_ShouldReturnNotFound_WhenAuthorNotFound() throws Exception {
        // Arrange
        when(authorService.getAuthorByUsername(USERNAME)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/recipes/create-form-data")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(AUTHOR_NOT_FOUND_FOR_USER_MESSAGE));
    }

    @Test
    @DisplayName("Получение данных формы редактирования - успешный случай")
    void getEditFormData_ShouldReturnFormData_WhenUserOwnsRecipe() throws Exception {
        // Arrange
        when(recipeService.getRecipeById(RECIPE_ID)).thenReturn(Optional.of(testRecipeDto));
        when(authorService.getAuthorByUsername(USERNAME)).thenReturn(Optional.of(testAuthorDto));
        when(categoryService.getAllCategories()).thenReturn(List.of(testCategoryDto));
        when(inventoryService.getAllInventory()).thenReturn(List.of(testInventoryDto));

        // Act & Assert
        mockMvc.perform(get("/api/recipes/edit-form-data/{id}", RECIPE_ID)
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.recipe.id").value(RECIPE_ID))
                .andExpect(jsonPath("$.data.categories").isArray())
                .andExpect(jsonPath("$.data.inventoryItems").isArray());
    }

    @Test
    @DisplayName("Получение данных формы редактирования - рецепт не найден")
    void getEditFormData_ShouldReturnNotFound_WhenRecipeNotFound() throws Exception {
        // Arrange
        String errorMessage = "Рецепт не найден";
        when(recipeService.getRecipeById(NON_EXISTING_RECIPE_ID))
                .thenThrow(new EntityNotFoundException(errorMessage));

        // Act & Assert
        mockMvc.perform(get("/api/recipes/edit-form-data/{id}", NON_EXISTING_RECIPE_ID)
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(errorMessage));
    }

    @Test
    @DisplayName("Получение моих рецептов - успешный случай")
    void getMyRecipes_ShouldReturnRecipes_WhenAuthenticated() throws Exception {
        // Arrange
        when(authorService.getAuthorByUsername(USERNAME)).thenReturn(Optional.of(testAuthorDto));
        when(recipeService.getRecipesByAuthor(AUTHOR_ID)).thenReturn(List.of(testRecipeSummaryDto));

        // Act & Assert
        mockMvc.perform(get("/api/recipes/my-recipes")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(RECIPE_ID))
                .andExpect(jsonPath("$.data[0].title").value(RECIPE_TITLE));
    }

    @Test
    @DisplayName("Создание рецепта - успешный случай (опубликованный)")
    @WithMockUser
    void createRecipe_ShouldCreateRecipe_WhenValidRequestAndPublished() throws Exception {
        // Arrange
        String requestBody = """
                {
                    "title": "Test Recipe",
                    "categoryId": 1,
                    "authorId": 1,
                    "ingredients": ["Ingredient 1", "Ingredient 2"],
                    "description": "Test recipe description",
                    "inventoryIds": [1, 2],
                    "published": true
                }
                """;

        when(authorService.getAuthorByUsername(USERNAME)).thenReturn(Optional.of(testAuthorDto));
        when(recipeService.createRecipeWithInventory(
                eq("Test Recipe"), eq(1L), eq(1L), anyList(), eq("Test recipe description"), anyList(), eq(true)
        )).thenReturn(testRecipeDto);

        // Act & Assert
        mockMvc.perform(post("/api/recipes")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(RECIPE_ID))
                .andExpect(jsonPath("$.message").value(RECIPE_CREATED_PUBLISHED_MESSAGE));
    }

    @Test
    @DisplayName("Создание рецепта - успешный случай (черновик)")
    @WithMockUser
    void createRecipe_ShouldCreateRecipe_WhenValidRequestAndDraft() throws Exception {
        // Arrange
        String requestBody = """
                {
                    "title": "Test Recipe",
                    "categoryId": 1,
                    "authorId": 1,
                    "ingredients": ["Ingredient 1", "Ingredient 2"],
                    "description": "Test recipe description",
                    "inventoryIds": [1, 2],
                    "published": false
                }
                """;

        when(authorService.getAuthorByUsername(USERNAME)).thenReturn(Optional.of(testAuthorDto));
        when(recipeService.createRecipeWithInventory(
                anyString(), anyLong(), anyLong(), anyList(), anyString(), anyList(), eq(false)
        )).thenReturn(testRecipeDto);

        // Act & Assert
        mockMvc.perform(post("/api/recipes")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(RECIPE_CREATED_DRAFT_MESSAGE));
    }

    @Test
    @DisplayName("Получение рецепта по ID - успешный случай")
    void getRecipeById_ShouldReturnRecipe_WhenRecipeExistsAndPublished() throws Exception {
        // Arrange
        when(recipeService.getRecipeById(RECIPE_ID)).thenReturn(Optional.of(testRecipeDto));

        // Act & Assert
        mockMvc.perform(get("/api/recipes/{id}", RECIPE_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(RECIPE_ID))
                .andExpect(jsonPath("$.data.title").value(RECIPE_TITLE))
                .andExpect(jsonPath("$.data.published").value(true));
    }

    @Test
    @DisplayName("Получение рецепта по ID - рецепт не найден")
    void getRecipeById_ShouldReturnNotFound_WhenRecipeNotExists() throws Exception {
        // Arrange
        String errorMessage = "Рецепт не найден";
        when(recipeService.getRecipeById(NON_EXISTING_RECIPE_ID))
                .thenThrow(new EntityNotFoundException(errorMessage));

        // Act & Assert
        mockMvc.perform(get("/api/recipes/{id}", NON_EXISTING_RECIPE_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(errorMessage));
    }

    @Test
    @DisplayName("Получение всех рецептов - успешный случай")
    void getAllRecipes_ShouldReturnRecipes() throws Exception {
        // Arrange
        when(recipeService.getAllPublishedRecipes()).thenReturn(List.of(testRecipeSummaryDto));

        // Act & Assert
        mockMvc.perform(get("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(RECIPE_ID));
    }

    @Test
    @DisplayName("Получение количества комментариев - успешный случай")
    void getCommentCount_ShouldReturnCount() throws Exception {
        // Arrange
        when(commentService.getCommentCountForRecipe(RECIPE_ID)).thenReturn(5);

        // Act & Assert
        mockMvc.perform(get("/api/recipes/{id}/comment-count", RECIPE_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(5));
    }

    @Test
    @DisplayName("Поиск рецептов по названию - успешный случай")
    void searchRecipesByTitle_ShouldReturnRecipes() throws Exception {
        // Arrange
        when(recipeService.searchPublishedRecipesByTitle("test")).thenReturn(List.of(testRecipeSummaryDto));

        // Act & Assert
        mockMvc.perform(get("/api/recipes/search/title")
                        .param("title", "test")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(RECIPE_ID));
    }

    @Test
    @DisplayName("Удаление рецепта - успешный случай")
    @WithMockUser
    void deleteRecipe_ShouldDeleteRecipe_WhenUserOwnsRecipe() throws Exception {
        // Arrange
        when(recipeService.getRecipeById(RECIPE_ID)).thenReturn(Optional.of(testRecipeDto));
        when(authorService.getAuthorByUsername(USERNAME)).thenReturn(Optional.of(testAuthorDto));

        // Act & Assert
        mockMvc.perform(delete("/api/recipes/{id}", RECIPE_ID)
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(RECIPE_DELETED_MESSAGE));
    }

    @Test
    @DisplayName("Публикация рецепта - успешный случай")
    @WithMockUser
    void publishRecipe_ShouldPublishRecipe_WhenUserOwnsRecipe() throws Exception {
        // Arrange
        when(recipeService.getRecipeById(RECIPE_ID)).thenReturn(Optional.of(testRecipeDto));
        when(authorService.getAuthorByUsername(USERNAME)).thenReturn(Optional.of(testAuthorDto));
        when(recipeService.publishRecipe(RECIPE_ID)).thenReturn(testRecipeDto);

        // Act & Assert
        mockMvc.perform(patch("/api/recipes/{id}/publish", RECIPE_ID)
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(RECIPE_PUBLISHED_MESSAGE));
    }

    @Test
    @DisplayName("Снятие с публикации рецепта - успешный случай")
    @WithMockUser
    void unpublishRecipe_ShouldUnpublishRecipe_WhenUserOwnsRecipe() throws Exception {
        // Arrange
        when(recipeService.getRecipeById(RECIPE_ID)).thenReturn(Optional.of(testRecipeDto));
        when(authorService.getAuthorByUsername(USERNAME)).thenReturn(Optional.of(testAuthorDto));
        when(recipeService.unpublishRecipe(RECIPE_ID)).thenReturn(testRecipeDto);

        // Act & Assert
        mockMvc.perform(patch("/api/recipes/{id}/unpublish", RECIPE_ID)
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(RECIPE_UNPUBLISHED_MESSAGE));
    }

    @Test
    @DisplayName("Создание рецепта - не аутентифицированный пользователь")
    void createRecipe_WhenUnauthenticated_ShouldReturnUnauthorized() throws Exception {
        String requestBody = """
                {
                    "title": "Test Recipe",
                    "categoryId": 1,
                    "authorId": 1,
                    "ingredients": ["Ingredient 1"],
                    "description": "Test description",
                    "published": true
                }
                """;

        mockMvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }
}