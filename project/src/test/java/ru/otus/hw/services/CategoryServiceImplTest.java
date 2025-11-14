package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.converters.CategoryConverter;
import ru.otus.hw.dto.CategoryDto;
import ru.otus.hw.dto.CategoryWithUsageDto;
import ru.otus.hw.exceptions.EntityAlreadyExistsException;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Category;
import ru.otus.hw.repositories.CategoryRepository;
import ru.otus.hw.util.MessageProvider;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты для CategoryServiceImpl")
class CategoryServiceImplTest {

    private static final Long EXISTING_CATEGORY_ID = 1L;
    private static final Long ANOTHER_CATEGORY_ID = 2L;
    private static final Long NON_EXISTING_CATEGORY_ID = 999L;
    private static final Long USED_CATEGORY_ID = 3L;

    private static final String DESSERTS_CATEGORY_NAME = "Десерты";
    private static final String SOUPS_CATEGORY_NAME = "Супы";
    private static final String UPDATED_CATEGORY_NAME = "Обновленные десерты";

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryConverter categoryConverter;

    @Mock
    private MessageProvider messageProvider;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category testCategory;
    private CategoryDto testCategoryDto;
    private CategoryWithUsageDto testCategoryWithUsageDto;

    @BeforeEach
    void setUp() {
        testCategory = new Category(DESSERTS_CATEGORY_NAME, "Сладкие блюда");
        testCategory.setId(EXISTING_CATEGORY_ID);

        testCategoryDto = new CategoryDto(EXISTING_CATEGORY_ID, DESSERTS_CATEGORY_NAME,
                "Сладкие блюда", LocalDateTime.now());
        testCategoryWithUsageDto = new CategoryWithUsageDto(EXISTING_CATEGORY_ID, DESSERTS_CATEGORY_NAME,
                "Сладкие блюда", LocalDateTime.now(), true, 5L);
    }

    @Test
    @DisplayName("Создание категории - успешное создание")
    void createCategory_ShouldCreateCategory_WhenValidData() {
        // Arrange
        String name = SOUPS_CATEGORY_NAME;
        String description = "Первые блюда";

        when(categoryRepository.existsByName(name)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);
        when(categoryConverter.toDto(testCategory)).thenReturn(testCategoryDto);

        // Act
        CategoryDto result = categoryService.createCategory(name, description);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(testCategoryDto);
        verify(categoryRepository).save(any(Category.class));
        verify(categoryRepository).existsByName(name);
    }

    @Test
    @DisplayName("Создание категории - категория уже существует")
    void createCategory_ShouldThrowException_WhenCategoryExists() {
        // Arrange
        String name = DESSERTS_CATEGORY_NAME;
        String description = "Сладкие блюда";

        when(categoryRepository.existsByName(name)).thenReturn(true);
        when(messageProvider.getMessage("category.already_exists", name))
                .thenReturn("Категория с именем Десерты уже существует");

        // Act & Assert
        assertThatThrownBy(() -> categoryService.createCategory(name, description))
                .isInstanceOf(EntityAlreadyExistsException.class)
                .hasMessage("Категория с именем Десерты уже существует");

        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Создание категории - пустое имя")
    void createCategory_ShouldThrowException_WhenNameIsEmpty() {
        // Arrange
        String name = "   ";
        String description = "Описание категории";

        when(messageProvider.getMessage("category.name_empty"))
                .thenReturn("Название категории не может быть пустым");

        // Act & Assert
        assertThatThrownBy(() -> categoryService.createCategory(name, description))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Название категории не может быть пустым");

        verify(categoryRepository, never()).existsByName(anyString());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Получение категории по ID - категория найдена")
    void getCategoryById_ShouldReturnCategory_WhenCategoryExists() {
        // Arrange
        when(categoryRepository.findById(EXISTING_CATEGORY_ID)).thenReturn(Optional.of(testCategory));
        when(categoryConverter.toDto(testCategory)).thenReturn(testCategoryDto);

        // Act
        Optional<CategoryDto> result = categoryService.getCategoryById(EXISTING_CATEGORY_ID);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testCategoryDto);
    }

    @Test
    @DisplayName("Получение категории по ID - категория не найдена")
    void getCategoryById_ShouldReturnEmpty_WhenCategoryNotExists() {
        // Arrange
        when(categoryRepository.findById(NON_EXISTING_CATEGORY_ID)).thenReturn(Optional.empty());

        // Act
        Optional<CategoryDto> result = categoryService.getCategoryById(NON_EXISTING_CATEGORY_ID);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Получение категории по имени - категория найдена")
    void getCategoryByName_ShouldReturnCategory_WhenCategoryExists() {
        // Arrange
        when(categoryRepository.findByName(DESSERTS_CATEGORY_NAME)).thenReturn(Optional.of(testCategory));
        when(categoryConverter.toDto(testCategory)).thenReturn(testCategoryDto);

        // Act
        Optional<CategoryDto> result = categoryService.getCategoryByName(DESSERTS_CATEGORY_NAME);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testCategoryDto);
    }

    @Test
    @DisplayName("Получение всех категорий - успешно")
    void getAllCategories_ShouldReturnAllCategories() {
        // Arrange
        List<Category> categories = List.of(testCategory);
        when(categoryRepository.findAll()).thenReturn(categories);
        when(categoryConverter.toDto(testCategory)).thenReturn(testCategoryDto);

        // Act
        List<CategoryDto> result = categoryService.getAllCategories();

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testCategoryDto);
    }

    @Test
    @DisplayName("Обновление категории - успешно")
    void updateCategory_ShouldUpdateCategory_WhenValidData() {
        // Arrange
        String newName = UPDATED_CATEGORY_NAME;
        String newDescription = "Обновленное описание";
        Category updatedCategory = new Category(newName, newDescription);
        updatedCategory.setId(EXISTING_CATEGORY_ID);
        CategoryDto updatedCategoryDto = new CategoryDto(EXISTING_CATEGORY_ID, newName,
                newDescription, LocalDateTime.now());

        when(categoryRepository.findById(EXISTING_CATEGORY_ID)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.existsByName(newName)).thenReturn(false);
        when(categoryRepository.save(testCategory)).thenReturn(updatedCategory);
        when(categoryConverter.toDto(updatedCategory)).thenReturn(updatedCategoryDto);

        // Act
        CategoryDto result = categoryService.updateCategory(EXISTING_CATEGORY_ID, newName, newDescription);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo(newName);
        assertThat(result.description()).isEqualTo(newDescription);
        verify(categoryRepository).save(testCategory);
    }

    @Test
    @DisplayName("Обновление категории - имя уже существует")
    void updateCategory_ShouldThrowException_WhenNameExists() {
        // Arrange
        String newName = SOUPS_CATEGORY_NAME;
        String newDescription = "Обновленное описание";

        when(categoryRepository.findById(EXISTING_CATEGORY_ID)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.existsByName(newName)).thenReturn(true);
        when(messageProvider.getMessage("category.already_exists", newName))
                .thenReturn("Категория Супы уже существует");

        // Act & Assert
        assertThatThrownBy(() -> categoryService.updateCategory(EXISTING_CATEGORY_ID, newName, newDescription))
                .isInstanceOf(EntityAlreadyExistsException.class)
                .hasMessage("Категория Супы уже существует");

        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Обновление категории - категория не найдена")
    void updateCategory_ShouldThrowException_WhenCategoryNotFound() {
        // Arrange
        String newDescription = "Обновленное описание";

        when(categoryRepository.findById(NON_EXISTING_CATEGORY_ID)).thenReturn(Optional.empty());
        when(messageProvider.getMessage("category.not_found", NON_EXISTING_CATEGORY_ID))
                .thenReturn("Категория не найдена");

        // Act & Assert
        assertThatThrownBy(() -> categoryService.updateCategory(NON_EXISTING_CATEGORY_ID, UPDATED_CATEGORY_NAME, newDescription))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Категория не найдена");

        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Удаление категории - успешно, когда не используется")
    void deleteCategory_ShouldDeleteCategory_WhenNotUsedInRecipes() {
        // Arrange
        when(categoryRepository.findById(EXISTING_CATEGORY_ID)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.isUsedInRecipes(EXISTING_CATEGORY_ID)).thenReturn(false);

        // Act
        categoryService.deleteCategory(EXISTING_CATEGORY_ID);

        // Assert
        verify(categoryRepository).delete(testCategory);
    }

    @Test
    @DisplayName("Удаление категории - ошибка, когда используется в рецептах")
    void deleteCategory_ShouldThrowException_WhenUsedInRecipes() {
        // Arrange
        when(categoryRepository.findById(USED_CATEGORY_ID)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.isUsedInRecipes(USED_CATEGORY_ID)).thenReturn(true);
        when(messageProvider.getMessage("category.cannot_delete_used"))
                .thenReturn("Невозможно удалить категорию, так как она используется в рецептах");

        // Act & Assert
        assertThatThrownBy(() -> categoryService.deleteCategory(USED_CATEGORY_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Невозможно удалить категорию, так как она используется в рецептах");

        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    @DisplayName("Удаление категории - категория не найдена")
    void deleteCategory_ShouldThrowException_WhenCategoryNotFound() {
        // Arrange
        when(categoryRepository.findById(NON_EXISTING_CATEGORY_ID)).thenReturn(Optional.empty());
        when(messageProvider.getMessage("category.not_found", NON_EXISTING_CATEGORY_ID))
                .thenReturn("Категория не найдена");

        // Act & Assert
        assertThatThrownBy(() -> categoryService.deleteCategory(NON_EXISTING_CATEGORY_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Категория не найдена");

        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    @DisplayName("Проверка использования категории в рецептах - используется")
    void isCategoryUsedInRecipes_ShouldReturnTrue_WhenCategoryUsed() {
        // Arrange
        when(categoryRepository.isUsedInRecipes(USED_CATEGORY_ID)).thenReturn(true);

        // Act
        boolean result = categoryService.isCategoryUsedInRecipes(USED_CATEGORY_ID);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Проверка использования категории в рецептах - не используется")
    void isCategoryUsedInRecipes_ShouldReturnFalse_WhenCategoryNotUsed() {
        // Arrange
        when(categoryRepository.isUsedInRecipes(EXISTING_CATEGORY_ID)).thenReturn(false);

        // Act
        boolean result = categoryService.isCategoryUsedInRecipes(EXISTING_CATEGORY_ID);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Получение количества рецептов по категории - успешно")
    void getRecipeCountByCategory_ShouldReturnCount() {
        // Arrange
        long expectedCount = 5L;
        when(categoryRepository.countRecipesByCategoryId(EXISTING_CATEGORY_ID)).thenReturn(expectedCount);

        // Act
        long result = categoryService.getRecipeCountByCategory(EXISTING_CATEGORY_ID);

        // Assert
        assertThat(result).isEqualTo(expectedCount);
    }

    @Test
    @DisplayName("Получение неиспользуемых категорий - успешно")
    void getUnusedCategories_ShouldReturnUnusedCategories() {
        // Arrange
        List<Category> unusedCategories = List.of(testCategory);
        when(categoryRepository.findUnusedCategories()).thenReturn(unusedCategories);
        when(categoryConverter.toDto(testCategory)).thenReturn(testCategoryDto);

        // Act
        List<CategoryDto> result = categoryService.getUnusedCategories();

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testCategoryDto);
    }

    @Test
    @DisplayName("Получение категорий с информацией об использовании - успешно")
    void getCategoriesWithUsage_ShouldReturnCategoriesWithUsage() {
        // Arrange
        List<Object[]> results = new ArrayList<>();
        results.add(new Object[]{testCategory, 5L});

        when(categoryRepository.findAllWithRecipeCount()).thenReturn(results);
        when(categoryConverter.toDtoWithUsage(testCategory, 5L)).thenReturn(testCategoryWithUsageDto);

        // Act
        List<CategoryWithUsageDto> result = categoryService.getCategoriesWithUsage();

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testCategoryWithUsageDto);
    }

    @Test
    @DisplayName("Получение статуса использования категорий - успешно")
    void getCategoriesUsageStatus_ShouldReturnUsageStatus() {
        // Arrange
        List<Long> categoryIds = List.of(EXISTING_CATEGORY_ID, ANOTHER_CATEGORY_ID);
        when(categoryRepository.isUsedInRecipes(EXISTING_CATEGORY_ID)).thenReturn(true);
        when(categoryRepository.isUsedInRecipes(ANOTHER_CATEGORY_ID)).thenReturn(false);

        // Act
        var result = categoryService.getCategoriesUsageStatus(categoryIds);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(EXISTING_CATEGORY_ID)).isTrue();
        assertThat(result.get(ANOTHER_CATEGORY_ID)).isFalse();
    }

    @Test
    @DisplayName("Создание категории - null имя")
    void createCategory_ShouldThrowException_WhenNameIsNull() {
        // Arrange
        String description = "Описание категории";

        when(messageProvider.getMessage("category.name_empty"))
                .thenReturn("Название категории не может быть пустым");

        // Act & Assert
        assertThatThrownBy(() -> categoryService.createCategory(null, description))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Название категории не может быть пустым");

        verify(categoryRepository, never()).existsByName(anyString());
        verify(categoryRepository, never()).save(any(Category.class));
    }
}