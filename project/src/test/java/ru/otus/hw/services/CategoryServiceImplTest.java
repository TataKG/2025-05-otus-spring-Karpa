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
import ru.otus.hw.models.Category;
import ru.otus.hw.repositories.CategoryRepository;
import ru.otus.hw.util.MessageProvider;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты для CategoryServiceImpl")
class CategoryServiceImplTest {

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
        testCategory = new Category("Test Category", "Test Description");
        testCategory.setId(1L);

        testCategoryDto = new CategoryDto(1L, "Test Category", "Test Description", LocalDateTime.now());
        testCategoryWithUsageDto = new CategoryWithUsageDto(1L, "Test Category", "Test Description",
                LocalDateTime.now(), true, 5L);
    }

    @Test
    @DisplayName("Создание категории - успешное создание")
    void createCategory_ShouldCreateCategory_WhenValidData() {
        // Given
        String name = "Test Category";
        String description = "Test Description";

        when(categoryRepository.existsByName(name)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);
        when(categoryConverter.toDto(testCategory)).thenReturn(testCategoryDto);

        // When
        CategoryDto result = categoryService.createCategory(name, description);

        // Then
        assertNotNull(result);
        assertEquals(testCategoryDto, result);
        verify(categoryRepository).save(any(Category.class));
        verify(categoryRepository).existsByName(name);
    }

    @Test
    @DisplayName("Создание категории - категория уже существует")
    void createCategory_ShouldThrowException_WhenCategoryExists() {
        // Given
        String name = "Test Category";
        String description = "Test Description";

        when(categoryRepository.existsByName(name)).thenReturn(true);

        // When & Then
        assertThrows(EntityAlreadyExistsException.class,
                () -> categoryService.createCategory(name, description));

        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Создание категории - пустое имя")
    void createCategory_ShouldThrowException_WhenNameIsEmpty() {
        // Given
        String name = "   ";
        String description = "Test Description";

        when(messageProvider.getMessage("category.name_empty")).thenReturn("Category name is empty");

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> categoryService.createCategory(name, description));

        verify(categoryRepository, never()).existsByName(anyString());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Получение категории по ID - категория найдена")
    void getCategoryById_ShouldReturnCategory_WhenCategoryExists() {
        // Given
        Long categoryId = 1L;
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
        when(categoryConverter.toDto(testCategory)).thenReturn(testCategoryDto);

        // When
        Optional<CategoryDto> result = categoryService.getCategoryById(categoryId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testCategoryDto, result.get());
    }

    @Test
    @DisplayName("Получение категории по ID - категория не найдена")
    void getCategoryById_ShouldReturnEmpty_WhenCategoryNotExists() {
        // Given
        Long categoryId = 1L;
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // When
        Optional<CategoryDto> result = categoryService.getCategoryById(categoryId);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Получение категории по имени - категория найдена")
    void getCategoryByName_ShouldReturnCategory_WhenCategoryExists() {
        // Given
        String categoryName = "Test Category";
        when(categoryRepository.findByName(categoryName)).thenReturn(Optional.of(testCategory));
        when(categoryConverter.toDto(testCategory)).thenReturn(testCategoryDto);

        // When
        Optional<CategoryDto> result = categoryService.getCategoryByName(categoryName);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testCategoryDto, result.get());
    }

    @Test
    @DisplayName("Получение всех категорий - успешно")
    void getAllCategories_ShouldReturnAllCategories() {
        // Given
        List<Category> categories = List.of(testCategory);
        when(categoryRepository.findAll()).thenReturn(categories);
        when(categoryConverter.toDto(testCategory)).thenReturn(testCategoryDto);

        // When
        List<CategoryDto> result = categoryService.getAllCategories();

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(testCategoryDto, result.get(0));
    }

    @Test
    @DisplayName("Обновление категории - успешно")
    void updateCategory_ShouldUpdateCategory_WhenValidData() {
        // Given
        Long categoryId = 1L;
        String newName = "Updated Category";
        String newDescription = "Updated Description";
        Category updatedCategory = new Category(newName, newDescription);
        updatedCategory.setId(1L);
        CategoryDto updatedCategoryDto = new CategoryDto(1L, newName, newDescription, LocalDateTime.now());

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.existsByName(newName)).thenReturn(false);
        when(categoryRepository.save(testCategory)).thenReturn(updatedCategory);
        when(categoryConverter.toDto(updatedCategory)).thenReturn(updatedCategoryDto);

        // When
        CategoryDto result = categoryService.updateCategory(categoryId, newName, newDescription);

        // Then
        assertNotNull(result);
        assertEquals(newName, result.name());
        assertEquals(newDescription, result.description());
        verify(categoryRepository).save(testCategory);
    }

    @Test
    @DisplayName("Обновление категории - имя уже существует")
    void updateCategory_ShouldThrowException_WhenNameExists() {
        // Given
        Long categoryId = 1L;
        String newName = "Existing Category";
        String newDescription = "Updated Description";

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.existsByName(newName)).thenReturn(true);

        // When & Then
        assertThrows(EntityAlreadyExistsException.class,
                () -> categoryService.updateCategory(categoryId, newName, newDescription));

        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Удаление категории - успешно, когда не используется")
    void deleteCategory_ShouldDeleteCategory_WhenNotUsedInRecipes() {
        // Given
        Long categoryId = 1L;
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.isUsedInRecipes(categoryId)).thenReturn(false);
         // When
        categoryService.deleteCategory(categoryId);

        // Then
        verify(categoryRepository).delete(testCategory);
    }

    @Test
    @DisplayName("Удаление категории - ошибка, когда используется в рецептах")
    void deleteCategory_ShouldThrowException_WhenUsedInRecipes() {
        // Given
        Long categoryId = 1L;
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.isUsedInRecipes(categoryId)).thenReturn(true);

        // When & Then
        assertThrows(IllegalStateException.class,
                () -> categoryService.deleteCategory(categoryId));

        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    @DisplayName("Проверка использования категории в рецептах - используется")
    void isCategoryUsedInRecipes_ShouldReturnTrue_WhenCategoryUsed() {
        // Given
        Long categoryId = 1L;
        when(categoryRepository.isUsedInRecipes(categoryId)).thenReturn(true);

        // When
        boolean result = categoryService.isCategoryUsedInRecipes(categoryId);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("Получение количества рецептов по категории - успешно")
    void getRecipeCountByCategory_ShouldReturnCount() {
        // Given
        Long categoryId = 1L;
        long expectedCount = 5L;
        when(categoryRepository.countRecipesByCategoryId(categoryId)).thenReturn(expectedCount);

        // When
        long result = categoryService.getRecipeCountByCategory(categoryId);

        // Then
        assertEquals(expectedCount, result);
    }

    @Test
    @DisplayName("Получение неиспользуемых категорий - успешно")
    void getUnusedCategories_ShouldReturnUnusedCategories() {
        // Given
        List<Category> unusedCategories = List.of(testCategory);
        when(categoryRepository.findUnusedCategories()).thenReturn(unusedCategories);
        when(categoryConverter.toDto(testCategory)).thenReturn(testCategoryDto);

        // When
        List<CategoryDto> result = categoryService.getUnusedCategories();

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(testCategoryDto, result.get(0));
    }

    @Test
    @DisplayName("Получение категорий с информацией об использовании - успешно")
    void getCategoriesWithUsage_ShouldReturnCategoriesWithUsage() {
        // Given
        List<Object[]> results = new ArrayList<>();
        results.add(new Object[]{testCategory, 5L});

        when(categoryRepository.findAllWithRecipeCount()).thenReturn(results);
        when(categoryConverter.toDtoWithUsage(testCategory, 5L)).thenReturn(testCategoryWithUsageDto);

        // When
        List<CategoryWithUsageDto> result = categoryService.getCategoriesWithUsage();

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(testCategoryWithUsageDto, result.get(0));
    }

    @Test
    @DisplayName("Получение статуса использования категорий - успешно")
    void getCategoriesUsageStatus_ShouldReturnUsageStatus() {
        // Given
        List<Long> categoryIds = List.of(1L, 2L);
        when(categoryRepository.isUsedInRecipes(1L)).thenReturn(true);
        when(categoryRepository.isUsedInRecipes(2L)).thenReturn(false);

        // When
        var result = categoryService.getCategoriesUsageStatus(categoryIds);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.get(1L));
        assertFalse(result.get(2L));
    }
}