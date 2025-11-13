package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.converters.InventoryConverter;
import ru.otus.hw.dto.InventoryDto;
import ru.otus.hw.dto.InventoryWithUsageDto;
import ru.otus.hw.exceptions.EntityAlreadyExistsException;
import ru.otus.hw.models.Inventory;
import ru.otus.hw.repositories.InventoryRepository;
import ru.otus.hw.util.MessageProvider;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты для InventoryServiceImpl")
class InventoryServiceImplTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryConverter inventoryConverter;

    @Mock
    private MessageProvider messageProvider;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private Inventory testInventory;
    private InventoryDto testInventoryDto;
    private InventoryWithUsageDto testInventoryWithUsageDto;

    @BeforeEach
    void setUp() {
        testInventory = new Inventory("Test Inventory", "Test Description");
        testInventory.setId(1L);

        testInventoryDto = new InventoryDto(1L, "Test Inventory", "Test Description", LocalDateTime.now());
        testInventoryWithUsageDto = new InventoryWithUsageDto(1L, "Test Inventory", "Test Description",
                LocalDateTime.now(), true, 5L);
    }

    @Test
    @DisplayName("Создание инвентаря - успешное создание")
    void createInventory_ShouldCreateInventory_WhenValidData() {
        // Given
        String name = "Test Inventory";
        String description = "Test Description";

        when(inventoryRepository.existsByName(name)).thenReturn(false);
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);
        when(inventoryConverter.toDto(testInventory)).thenReturn(testInventoryDto);

        // When
        InventoryDto result = inventoryService.createInventory(name, description);

        // Then
        assertNotNull(result);
        assertEquals(testInventoryDto, result);
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    @DisplayName("Создание инвентаря - инвентарь уже существует")
    void createInventory_ShouldThrowException_WhenInventoryExists() {
        // Given
        String name = "Test Inventory";
        String description = "Test Description";

        when(inventoryRepository.existsByName(name)).thenReturn(true);

        // When & Then
        assertThrows(EntityAlreadyExistsException.class,
                () -> inventoryService.createInventory(name, description));

        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    @DisplayName("Создание инвентаря - пустое имя")
    void createInventory_ShouldThrowException_WhenNameIsEmpty() {
        // Given
        String name = "   ";
        String description = "Test Description";
        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> inventoryService.createInventory(name, description));

        verify(inventoryRepository, never()).existsByName(anyString());
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    @DisplayName("Получение инвентаря по ID - инвентарь найден")
    void getInventoryById_ShouldReturnInventory_WhenInventoryExists() {
        // Given
        Long inventoryId = 1L;
        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.of(testInventory));
        when(inventoryConverter.toDto(testInventory)).thenReturn(testInventoryDto);

        // When
        Optional<InventoryDto> result = inventoryService.getInventoryById(inventoryId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testInventoryDto, result.get());
    }

    @Test
    @DisplayName("Получение инвентаря по ID с рецептами - инвентарь найден")
    void getInventoryByIdWithRecipes_ShouldReturnInventory_WhenInventoryExists() {
        // Given
        Long inventoryId = 1L;
        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.of(testInventory));
        when(inventoryConverter.toDto(testInventory)).thenReturn(testInventoryDto);

        // When
        Optional<InventoryDto> result = inventoryService.getInventoryByIdWithRecipes(inventoryId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testInventoryDto, result.get());
    }

    @Test
    @DisplayName("Получение инвентаря по ID - инвентарь не найден")
    void getInventoryById_ShouldReturnEmpty_WhenInventoryNotExists() {
        // Given
        Long inventoryId = 1L;
        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.empty());

        // When
        Optional<InventoryDto> result = inventoryService.getInventoryById(inventoryId);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Поиск инвентаря по имени - с поисковым запросом")
    void getInventoryByNameContaining_ShouldReturnMatchingInventory_WhenSearchQueryProvided() {
        // Given
        String searchName = "Test";
        List<Inventory> inventoryList = List.of(testInventory);
        when(inventoryRepository.findByNameContainingIgnoreCase(searchName)).thenReturn(inventoryList);
        when(inventoryConverter.toDto(testInventory)).thenReturn(testInventoryDto);

        // When
        List<InventoryDto> result = inventoryService.getInventoryByNameContaining(searchName);

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(testInventoryDto, result.get(0));
    }

    @Test
    @DisplayName("Поиск инвентаря по имени - без поискового запроса")
    void getInventoryByNameContaining_ShouldReturnAllInventory_WhenNoSearchQuery() {
        // Given
        List<Inventory> inventoryList = List.of(testInventory);
        when(inventoryRepository.findAllByOrderByNameAsc()).thenReturn(inventoryList);
        when(inventoryConverter.toDto(testInventory)).thenReturn(testInventoryDto);

        // When
        List<InventoryDto> result = inventoryService.getInventoryByNameContaining(null);

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(testInventoryDto, result.get(0));
    }

    @Test
    @DisplayName("Получение всего инвентаря - успешно")
    void getAllInventory_ShouldReturnAllInventory() {
        // Given
        List<Inventory> inventoryList = List.of(testInventory);
        when(inventoryRepository.findAllByOrderByNameAsc()).thenReturn(inventoryList);
        when(inventoryConverter.toDto(testInventory)).thenReturn(testInventoryDto);

        // When
        List<InventoryDto> result = inventoryService.getAllInventory();

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(testInventoryDto, result.get(0));
    }

    @Test
    @DisplayName("Получение инвентаря по списку имен - успешно")
    void getInventoryByNames_ShouldReturnMatchingInventory() {
        // Given
        List<String> names = List.of("Test Inventory");
        List<Inventory> inventoryList = List.of(testInventory);
        when(inventoryRepository.findByNames(names)).thenReturn(inventoryList);
        when(inventoryConverter.toDto(testInventory)).thenReturn(testInventoryDto);

        // When
        List<InventoryDto> result = inventoryService.getInventoryByNames(names);

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(testInventoryDto, result.get(0));
    }

    @Test
    @DisplayName("Получение инвентаря по ID рецепта - успешно")
    void getInventoryByRecipeId_ShouldReturnInventoryForRecipe() {
        // Given
        Long recipeId = 1L;
        List<Inventory> inventoryList = List.of(testInventory);
        when(inventoryRepository.findByRecipeId(recipeId)).thenReturn(inventoryList);
        when(inventoryConverter.toDto(testInventory)).thenReturn(testInventoryDto);

        // When
        List<InventoryDto> result = inventoryService.getInventoryByRecipeId(recipeId);

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(testInventoryDto, result.get(0));
    }

    @Test
    @DisplayName("Обновление инвентаря - успешно")
    void updateInventory_ShouldUpdateInventory_WhenInventoryExists() {
        // Given
        Long inventoryId = 1L;
        String newDescription = "Updated Description";
        Inventory updatedInventory = new Inventory("Test Inventory", newDescription);
        updatedInventory.setId(1L);
        InventoryDto updatedInventoryDto = new InventoryDto(1L, "Test Inventory", newDescription, LocalDateTime.now());

        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(testInventory)).thenReturn(updatedInventory);
        when(inventoryConverter.toDto(updatedInventory)).thenReturn(updatedInventoryDto);
        // When
        InventoryDto result = inventoryService.updateInventory(inventoryId, newDescription);

        // Then
        assertNotNull(result);
        assertEquals(newDescription, result.description());
        verify(inventoryRepository).save(testInventory);
    }

    @Test
    @DisplayName("Удаление инвентаря - успешно, когда не используется")
    void deleteInventory_ShouldDeleteInventory_WhenNotUsedInRecipes() {
        // Given
        Long inventoryId = 1L;
        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.isUsedInRecipes(inventoryId)).thenReturn(false);
        // When
        inventoryService.deleteInventory(inventoryId);

        // Then
        verify(inventoryRepository).delete(testInventory);
    }

    @Test
    @DisplayName("Удаление инвентаря - ошибка, когда используется в рецептах")
    void deleteInventory_ShouldThrowException_WhenUsedInRecipes() {
        // Given
        Long inventoryId = 1L;
        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.isUsedInRecipes(inventoryId)).thenReturn(true);
        // When & Then
        assertThrows(IllegalStateException.class,
                () -> inventoryService.deleteInventory(inventoryId));

        verify(inventoryRepository, never()).delete(any(Inventory.class));
    }

    @Test
    @DisplayName("Проверка использования инвентаря в рецептах - используется")
    void isInventoryUsedInRecipes_ShouldReturnTrue_WhenInventoryUsed() {
        // Given
        Long inventoryId = 1L;
        when(inventoryRepository.isUsedInRecipes(inventoryId)).thenReturn(true);

        // When
        boolean result = inventoryService.isInventoryUsedInRecipes(inventoryId);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("Получение количества рецептов по инвентарю - успешно")
    void getRecipeCountByInventory_ShouldReturnCount() {
        // Given
        Long inventoryId = 1L;
        long expectedCount = 5L;
        when(inventoryRepository.countRecipesByInventoryId(inventoryId)).thenReturn(expectedCount);

        // When
        long result = inventoryService.getRecipeCountByInventory(inventoryId);

        // Then
        assertEquals(expectedCount, result);
    }

    @Test
    @DisplayName("Получение неиспользуемого инвентаря - успешно")
    void getUnusedInventory_ShouldReturnUnusedInventory() {
        // Given
        List<Inventory> unusedInventory = List.of(testInventory);
        when(inventoryRepository.findUnusedInventory()).thenReturn(unusedInventory);
        when(inventoryConverter.toDto(testInventory)).thenReturn(testInventoryDto);

        // When
        List<InventoryDto> result = inventoryService.getUnusedInventory();

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(testInventoryDto, result.get(0));
    }

    @Test
    @DisplayName("Получение инвентаря с информацией об использовании - успешно")
    void getInventoryWithUsage_ShouldReturnInventoryWithUsage() {
        // Given
        List<Inventory> inventoryList = List.of(testInventory);
        when(inventoryRepository.findAllByOrderByNameAsc()).thenReturn(inventoryList);
        when(inventoryConverter.toDto(testInventory)).thenReturn(testInventoryDto);
        when(inventoryRepository.countRecipesByInventoryId(1L)).thenReturn(5L);
        when(inventoryConverter.toDtoWithUsageFromDto(testInventoryDto, 5L)).thenReturn(testInventoryWithUsageDto);

        // When
        List<InventoryWithUsageDto> result = inventoryService.getInventoryWithUsage();

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(testInventoryWithUsageDto, result.get(0));
    }

    @Test
    @DisplayName("Получение инвентаря с информацией об использовании опубликованных рецептов - пустой список")
    void getInventoryWithPublishedUsage_ShouldReturnEmptyList() {
        // When
        List<InventoryWithUsageDto> result = inventoryService.getInventoryWithPublishedUsage();

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Получение статуса использования инвентаря - успешно")
    void getInventoryUsageStatus_ShouldReturnUsageStatus() {
        // Given
        List<Long> inventoryIds = List.of(1L, 2L);
        when(inventoryRepository.isUsedInRecipes(1L)).thenReturn(true);
        when(inventoryRepository.isUsedInRecipes(2L)).thenReturn(false);

        // When
        Map<Long, Boolean> result = inventoryService.getInventoryUsageStatus(inventoryIds);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.get(1L));
        assertFalse(result.get(2L));
    }

    @Test
    @DisplayName("Получение инвентаря по ID для внутреннего использования - успешно")
    void getInventoryByIdsForInternalUse_ShouldReturnInventory_WhenIdsProvided() {
        // Given
        List<Long> inventoryIds = List.of(1L, 2L);
        List<Inventory> inventoryList = List.of(testInventory);
        when(inventoryRepository.findAllById(inventoryIds)).thenReturn(inventoryList);
        when(inventoryConverter.toDto(testInventory)).thenReturn(testInventoryDto);

        // When
        List<InventoryDto> result = inventoryService.getInventoryByIdsForInternalUse(inventoryIds);

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(testInventoryDto, result.get(0));
    }

    @Test
    @DisplayName("Получение инвентаря по ID для внутреннего использования - пустой список при null")
    void getInventoryByIdsForInternalUse_ShouldReturnEmptyList_WhenIdsNull() {
        // When
        List<InventoryDto> result = inventoryService.getInventoryByIdsForInternalUse(null);

        // Then
        assertTrue(result.isEmpty());
        verify(inventoryRepository, never()).findAllById(any());
    }

    @Test
    @DisplayName("Получение инвентаря по ID для внутреннего использования - пустой список при пустом списке")
    void getInventoryByIdsForInternalUse_ShouldReturnEmptyList_WhenIdsEmpty() {
        // When
        List<InventoryDto> result = inventoryService.getInventoryByIdsForInternalUse(List.of());

        // Then
        assertTrue(result.isEmpty());
        verify(inventoryRepository, never()).findAllById(any());
    }

    @Test
    @DisplayName("Получение инвентаря по имени с рецептами - успешно")
    void getInventoryByNameContainingWithRecipes_ShouldReturnInventory() {
        // Given
        String searchName = "Test";
        List<Inventory> inventoryList = List.of(testInventory);
        when(inventoryRepository.findByNameContainingIgnoreCase(searchName)).thenReturn(inventoryList);
        when(inventoryConverter.toDto(testInventory)).thenReturn(testInventoryDto);

        // When
        List<InventoryDto> result = inventoryService.getInventoryByNameContainingWithRecipes(searchName);

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(testInventoryDto, result.get(0));
    }
}