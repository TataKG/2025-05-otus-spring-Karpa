package ru.otus.hw.controllers.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.config.SecurityConfig;
import ru.otus.hw.dto.CategoryDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.CategoryService;
import ru.otus.hw.utils.MessageProvider;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
@Import(SecurityConfig.class)
class CategoryControllerTest {

    private static final Long EXISTING_CATEGORY_ID = 1L;
    private static final Long NON_EXISTING_CATEGORY_ID = 999L;
    private static final String EXISTING_CATEGORY_NAME = "Супы";
    private static final String NON_EXISTING_CATEGORY_NAME = "Несуществующая категория";
    private static final String CATEGORY_DESCRIPTION = "Описание категории супов";

    private static final String CATEGORY_NOT_FOUND_MESSAGE = "Категория не найдена";
    private static final String CATEGORY_NOT_FOUND_NAME_MESSAGE = "Категория с именем не найдена";
    private static final String CATEGORIES_LOAD_ERROR_MESSAGE = "Ошибка загрузки категорий";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private MessageProvider messageProvider;

    private CategoryDto testCategoryDto;

    @BeforeEach
    void setUp() {
        testCategoryDto = new CategoryDto(
                EXISTING_CATEGORY_ID,
                EXISTING_CATEGORY_NAME,
                CATEGORY_DESCRIPTION,
                LocalDateTime.now()
        );

        // Mock message provider responses
        when(messageProvider.getMessage("category.not_found", NON_EXISTING_CATEGORY_ID))
                .thenReturn(CATEGORY_NOT_FOUND_MESSAGE);
        when(messageProvider.getMessage("category.not_found.name", NON_EXISTING_CATEGORY_NAME))
                .thenReturn(CATEGORY_NOT_FOUND_NAME_MESSAGE);
        when(messageProvider.getMessage("categories.load_error"))
                .thenReturn(CATEGORIES_LOAD_ERROR_MESSAGE);
    }

    @Test
    @DisplayName("Получение категории по ID - успешный случай")
    @WithMockUser
    void getCategoryById_WhenCategoryExists_ShouldReturnCategory() throws Exception {
        when(categoryService.getCategoryById(EXISTING_CATEGORY_ID))
                .thenReturn(Optional.of(testCategoryDto));

        mockMvc.perform(get("/api/categories/{id}", EXISTING_CATEGORY_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(EXISTING_CATEGORY_ID))
                .andExpect(jsonPath("$.data.name").value(EXISTING_CATEGORY_NAME))
                .andExpect(jsonPath("$.data.description").value(CATEGORY_DESCRIPTION));
    }

    @Test
    @DisplayName("Получение категории по ID - категория не найдена")
    @WithMockUser
    void getCategoryById_WhenCategoryNotExists_ShouldReturnNotFound() throws Exception {
        when(categoryService.getCategoryById(NON_EXISTING_CATEGORY_ID))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/categories/{id}", NON_EXISTING_CATEGORY_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(CATEGORY_NOT_FOUND_MESSAGE));
    }

    @Test
    @DisplayName("Получение категории по имени - успешный случай")
    @WithMockUser
    void getCategoryByName_WhenCategoryExists_ShouldReturnCategory() throws Exception {
        when(categoryService.getCategoryByName(EXISTING_CATEGORY_NAME))
                .thenReturn(Optional.of(testCategoryDto));

        mockMvc.perform(get("/api/categories/name/{name}", EXISTING_CATEGORY_NAME)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(EXISTING_CATEGORY_ID))
                .andExpect(jsonPath("$.data.name").value(EXISTING_CATEGORY_NAME))
                .andExpect(jsonPath("$.data.description").value(CATEGORY_DESCRIPTION));
    }

    @Test
    @DisplayName("Получение категории по имени - категория не найдена")
    @WithMockUser
    void getCategoryByName_WhenCategoryNotExists_ShouldReturnNotFound() throws Exception {
        when(categoryService.getCategoryByName(NON_EXISTING_CATEGORY_NAME))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/categories/name/{name}", NON_EXISTING_CATEGORY_NAME)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(CATEGORY_NOT_FOUND_NAME_MESSAGE));
    }

    @Test
    @DisplayName("Получение всех категорий - успешный случай")
    @WithMockUser
    void getAllCategories_WhenCategoriesExist_ShouldReturnCategoriesList() throws Exception {
        List<CategoryDto> categories = List.of(testCategoryDto);
        when(categoryService.getAllCategories()).thenReturn(categories);

        mockMvc.perform(get("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(EXISTING_CATEGORY_ID))
                .andExpect(jsonPath("$.data[0].name").value(EXISTING_CATEGORY_NAME))
                .andExpect(jsonPath("$.data[0].description").value(CATEGORY_DESCRIPTION));
    }

    @Test
    @DisplayName("Получение всех категорий - пустой список")
    @WithMockUser
    void getAllCategories_WhenNoCategories_ShouldReturnEmptyList() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(List.of());

        mockMvc.perform(get("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("Получение всех категорий - внутренняя ошибка сервера")
    @WithMockUser
    void getAllCategories_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        when(categoryService.getAllCategories())
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(CATEGORIES_LOAD_ERROR_MESSAGE));
    }

    @Test
    @DisplayName("Проверка существования категории - категория существует")
    @WithMockUser
    void checkCategoryExists_WhenCategoryExists_ShouldReturnTrue() throws Exception {
        when(categoryService.getCategoryByName(EXISTING_CATEGORY_NAME))
                .thenReturn(Optional.of(testCategoryDto));

        mockMvc.perform(get("/api/categories/exists/{name}", EXISTING_CATEGORY_NAME)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("Проверка существования категории - категория не существует")
    @WithMockUser
    void checkCategoryExists_WhenCategoryNotExists_ShouldReturnFalse() throws Exception {
        when(categoryService.getCategoryByName(NON_EXISTING_CATEGORY_NAME))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/categories/exists/{name}", NON_EXISTING_CATEGORY_NAME)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    @DisplayName("Получение категории по ID - EntityNotFoundException обрабатывается корректно")
    @WithMockUser
    void getCategoryById_WhenEntityNotFoundExceptionThrown_ShouldReturnNotFound() throws Exception {
        when(categoryService.getCategoryById(EXISTING_CATEGORY_ID))
                .thenThrow(new EntityNotFoundException(CATEGORY_NOT_FOUND_MESSAGE));

        mockMvc.perform(get("/api/categories/{id}", EXISTING_CATEGORY_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(CATEGORY_NOT_FOUND_MESSAGE));
    }

    @Test
    @DisplayName("Получение категории по имени - EntityNotFoundException обрабатывается корректно")
    @WithMockUser
    void getCategoryByName_WhenEntityNotFoundExceptionThrown_ShouldReturnNotFound() throws Exception {
        when(categoryService.getCategoryByName(EXISTING_CATEGORY_NAME))
                .thenThrow(new EntityNotFoundException(CATEGORY_NOT_FOUND_NAME_MESSAGE));

        mockMvc.perform(get("/api/categories/name/{name}", EXISTING_CATEGORY_NAME)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(CATEGORY_NOT_FOUND_NAME_MESSAGE));
    }

    @Test
    @DisplayName("Доступ к категориям - не аутентифицированный пользователь")
    void getAllCategories_WhenUnauthenticated_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Получение категории по ID - внутренняя ошибка сервера")
    @WithMockUser
    void getCategoryById_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        when(categoryService.getCategoryById(anyLong()))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/categories/{id}", EXISTING_CATEGORY_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Получение категории по имени - внутренняя ошибка сервера")
    @WithMockUser
    void getCategoryByName_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        when(categoryService.getCategoryByName(anyString()))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/categories/name/{name}", EXISTING_CATEGORY_NAME)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}