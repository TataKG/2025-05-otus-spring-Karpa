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
import ru.otus.hw.dto.CategoryDto;
import ru.otus.hw.exceptions.EntityAlreadyExistsException;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.CategoryService;
import ru.otus.hw.utils.MessageProvider;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты контроллера администратора для работы с категориями")
class AdminCategoryControllerTest {

    private static final Long EXISTING_CATEGORY_ID = 1L;
    private static final Long ANOTHER_CATEGORY_ID = 2L;
    private static final Long NON_EXISTING_CATEGORY_ID = 999L;
    private static final Long USED_CATEGORY_ID = 3L;

    private static final String DESSERTS_CATEGORY_NAME = "Десерты";
    private static final String SOUPS_CATEGORY_NAME = "Супы";
    private static final String UPDATED_CATEGORY_NAME = "Обновленные десерты";

    @Mock
    private CategoryService categoryService;

    @Mock
    private MessageProvider messageProvider;

    @InjectMocks
    private AdminCategoryController adminCategoryController;

    @BeforeEach
    void setUp() {
        UserDetails adminUser = new User("admin", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(adminUser, null, adminUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private CategoryDto createDessertsCategory() {
        return createTestCategory(EXISTING_CATEGORY_ID, DESSERTS_CATEGORY_NAME, "Сладкие блюда");
    }

    private CategoryDto createSoupsCategory() {
        return createTestCategory(ANOTHER_CATEGORY_ID, SOUPS_CATEGORY_NAME, "Первые блюда");
    }

    private CategoryDto createTestCategory(Long id, String name, String description) {
        return new CategoryDto(
                id,
                name,
                description,
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("Получение всех категорий - успешное выполнение")
    void getAllCategories_Success() {
        // Arrange
        CategoryDto dessertsCategory = createDessertsCategory();
        CategoryDto soupsCategory = createSoupsCategory();
        List<CategoryDto> categories = List.of(dessertsCategory, soupsCategory);
        when(categoryService.getAllCategories()).thenReturn(categories);

        // Act
        ResponseEntity<ApiResponse<List<CategoryDto>>> response = adminCategoryController.getAllCategories();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data()).isEqualTo(categories);
        assertThat(response.getBody().data()).hasSize(2);
        verify(categoryService).getAllCategories();
    }

    @Test
    @DisplayName("Создание категории - успешное создание")
    void createCategory_Success() {
        // Arrange
        AdminCategoryController.CategoryRequest request =
                new AdminCategoryController.CategoryRequest(SOUPS_CATEGORY_NAME, "Первые блюда");
        CategoryDto categoryDto = createSoupsCategory();

        when(categoryService.createCategory(SOUPS_CATEGORY_NAME, "Первые блюда")).thenReturn(categoryDto);
        when(messageProvider.getMessage("category.created")).thenReturn("Категория успешно создана");

        // Act
        ResponseEntity<ApiResponse<CategoryDto>> response = adminCategoryController.createCategory(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data()).isEqualTo(categoryDto);
        assertThat(response.getBody().message()).isEqualTo("Категория успешно создана");
        verify(categoryService).createCategory(SOUPS_CATEGORY_NAME, "Первые блюда");
    }

    @Test
    @DisplayName("Создание категории - категория уже существует")
    void createCategory_EntityAlreadyExists() {
        // Arrange
        AdminCategoryController.CategoryRequest request =
                new AdminCategoryController.CategoryRequest(DESSERTS_CATEGORY_NAME, "Сладкие блюда");
        when(categoryService.createCategory(DESSERTS_CATEGORY_NAME, "Сладкие блюда"))
                .thenThrow(new EntityAlreadyExistsException("Категория уже существует"));

        // Act
        ResponseEntity<ApiResponse<CategoryDto>> response = adminCategoryController.createCategory(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().message()).isEqualTo("Категория уже существует");
    }

    @Test
    @DisplayName("Обновление категории - успешное обновление")
    void updateCategory_Success() {
        // Arrange
        AdminCategoryController.CategoryRequest request =
                new AdminCategoryController.CategoryRequest(
                        UPDATED_CATEGORY_NAME,
                        "Обновленное описание");
        CategoryDto categoryDto = createTestCategory(
                EXISTING_CATEGORY_ID,
                UPDATED_CATEGORY_NAME,
                "Обновленное описание"
        );

        when(categoryService.updateCategory(
                EXISTING_CATEGORY_ID,
                UPDATED_CATEGORY_NAME,
                "Обновленное описание"
        )).thenReturn(categoryDto);
        when(messageProvider.getMessage("category.updated")).thenReturn("Категория успешно обновлена");

        // Act
        ResponseEntity<ApiResponse<CategoryDto>> response = adminCategoryController.updateCategory(EXISTING_CATEGORY_ID, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data()).isEqualTo(categoryDto);
        assertThat(response.getBody().message()).isEqualTo("Категория успешно обновлена");
        verify(categoryService).updateCategory(EXISTING_CATEGORY_ID, UPDATED_CATEGORY_NAME, "Обновленное описание");
    }

    @Test
    @DisplayName("Обновление категории - категория не найдена")
    void updateCategory_EntityNotFound() {
        // Arrange
        AdminCategoryController.CategoryRequest request =
                new AdminCategoryController.CategoryRequest("Новое имя", "Новое описание");
        when(categoryService.updateCategory(NON_EXISTING_CATEGORY_ID, "Новое имя", "Новое описание"))
                .thenThrow(new EntityNotFoundException("Категория не найдена"));

        // Act
        ResponseEntity<ApiResponse<CategoryDto>> response = adminCategoryController.updateCategory(NON_EXISTING_CATEGORY_ID, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().message()).isEqualTo("Категория не найдена");
    }

    @Test
    @DisplayName("Обновление категории - категория уже существует")
    void updateCategory_EntityAlreadyExists() {
        // Arrange
        AdminCategoryController.CategoryRequest request =
                new AdminCategoryController.CategoryRequest("Супы", "Описание");
        when(categoryService.updateCategory(EXISTING_CATEGORY_ID, "Супы", "Описание"))
                .thenThrow(new EntityAlreadyExistsException("Категория с таким названием уже существует"));

        // Act
        ResponseEntity<ApiResponse<CategoryDto>> response = adminCategoryController.updateCategory(EXISTING_CATEGORY_ID, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().message()).isEqualTo("Категория с таким названием уже существует");
    }

    @Test
    @DisplayName("Удаление категории - успешное удаление")
    void deleteCategory_Success() {
        // Arrange
        when(messageProvider.getMessage("category.deleted")).thenReturn("Категория успешно удалена");

        // Act
        ResponseEntity<ApiResponse<Void>> response = adminCategoryController.deleteCategory(EXISTING_CATEGORY_ID);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().message()).isEqualTo("Категория успешно удалена");
        verify(categoryService).deleteCategory(EXISTING_CATEGORY_ID);
    }

    @Test
    @DisplayName("Удаление категории - категория не найдена")
    void deleteCategory_EntityNotFound() {
        // Arrange
        doThrow(new EntityNotFoundException("Категория не найдена"))
                .when(categoryService).deleteCategory(NON_EXISTING_CATEGORY_ID);

        // Act
        ResponseEntity<ApiResponse<Void>> response = adminCategoryController.deleteCategory(NON_EXISTING_CATEGORY_ID);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().message()).isEqualTo("Категория не найдена");
    }

    @Test
    @DisplayName("Удаление категории - конфликт при удалении используемой категории")
    void deleteCategory_IllegalStateException() {
        // Arrange
        doThrow(new IllegalStateException("Невозможно удалить категорию, так как она используется в рецептах"))
                .when(categoryService).deleteCategory(USED_CATEGORY_ID);

        // Act
        ResponseEntity<ApiResponse<Void>> response = adminCategoryController.deleteCategory(USED_CATEGORY_ID);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().message()).isEqualTo("Невозможно удалить категорию, так как она используется в рецептах");
    }

    @Test
    @DisplayName("Получение информации об использовании категории - успешное выполнение")
    void getCategoryUsage_Success() {
        // Arrange
        when(categoryService.isCategoryUsedInRecipes(EXISTING_CATEGORY_ID)).thenReturn(true);
        when(categoryService.getRecipeCountByCategory(EXISTING_CATEGORY_ID)).thenReturn(5L);

        // Act
        ResponseEntity<ApiResponse<AdminCategoryController.CategoryUsageResponse>> response =
                adminCategoryController.getCategoryUsage(EXISTING_CATEGORY_ID);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data().isUsed()).isTrue();
        assertThat(response.getBody().data().recipeCount()).isEqualTo(5L);
        verify(categoryService).isCategoryUsedInRecipes(EXISTING_CATEGORY_ID);
        verify(categoryService).getRecipeCountByCategory(EXISTING_CATEGORY_ID);
    }

    @Test
    @DisplayName("Получение информации об использовании категории - категория не используется")
    void getCategoryUsage_CategoryNotUsed() {
        // Arrange
        when(categoryService.isCategoryUsedInRecipes(ANOTHER_CATEGORY_ID)).thenReturn(false);
        when(categoryService.getRecipeCountByCategory(ANOTHER_CATEGORY_ID)).thenReturn(0L);

        // Act
        ResponseEntity<ApiResponse<AdminCategoryController.CategoryUsageResponse>> response =
                adminCategoryController.getCategoryUsage(ANOTHER_CATEGORY_ID);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data().isUsed()).isFalse();
        assertThat(response.getBody().data().recipeCount()).isEqualTo(0L);
        verify(categoryService).isCategoryUsedInRecipes(ANOTHER_CATEGORY_ID);
        verify(categoryService).getRecipeCountByCategory(ANOTHER_CATEGORY_ID);
    }

    @Test
    @DisplayName("Получение информации об использовании категории - ошибка")
    void getCategoryUsage_Exception() {
        // Arrange
        when(categoryService.isCategoryUsedInRecipes(NON_EXISTING_CATEGORY_ID)).thenThrow(new RuntimeException("Ошибка"));
        when(messageProvider.getMessage("category.not_found", NON_EXISTING_CATEGORY_ID)).thenReturn("Категория не найдена: " + NON_EXISTING_CATEGORY_ID);

        // Act
        ResponseEntity<ApiResponse<AdminCategoryController.CategoryUsageResponse>> response =
                adminCategoryController.getCategoryUsage(NON_EXISTING_CATEGORY_ID);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().message()).contains(String.valueOf(NON_EXISTING_CATEGORY_ID));
    }

    @Test
    @DisplayName("Получение информации об использовании категории - категория не найдена при проверке использования")
    void getCategoryUsage_EntityNotFound() {
        // Arrange
        String expectedMessage = "Категория не найдена: " + NON_EXISTING_CATEGORY_ID;
        when(categoryService.isCategoryUsedInRecipes(NON_EXISTING_CATEGORY_ID))
                .thenThrow(new EntityNotFoundException("Категория не найдена"));
        when(messageProvider.getMessage("category.not_found", NON_EXISTING_CATEGORY_ID))
                .thenReturn(expectedMessage);

        // Act
        ResponseEntity<ApiResponse<AdminCategoryController.CategoryUsageResponse>> response =
                adminCategoryController.getCategoryUsage(NON_EXISTING_CATEGORY_ID);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().message()).isEqualTo(expectedMessage);
    }
}