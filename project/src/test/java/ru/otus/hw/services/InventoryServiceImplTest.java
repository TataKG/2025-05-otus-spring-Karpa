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
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Inventory;
import ru.otus.hw.repositories.InventoryRepository;
import ru.otus.hw.utils.MessageProvider;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты для InventoryServiceImpl")
class InventoryServiceImplTest {

    private static final Long EXISTING_INVENTORY_ID = 1L;
    private static final Long ANOTHER_INVENTORY_ID = 2L;
    private static final Long NON_EXISTING_INVENTORY_ID = 999L;
    private static final Long USED_INVENTORY_ID = 3L;
    private static final Long EXISTING_RECIPE_ID = 1L;

    private static final String OVEN_NAME = "Духовка";
    private static final String BLENDER_NAME = "Блендер";
    private static final String UPDATED_DESCRIPTION = "Обновленное описание";

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
        testInventory = new Inventory(OVEN_NAME, "Электрическая духовка");
        testInventory.setId(EXISTING_INVENTORY_ID);

        testInventoryDto = new InventoryDto(EXISTING_INVENTORY_ID, OVEN_NAME,
                "Электрическая духовка", LocalDateTime.now());
        testInventoryWithUsageDto = new InventoryWithUsageDto(EXISTING_INVENTORY_ID, OVEN_NAME,
                "Электрическая духовка", LocalDateTime.now(), true, 5L);
    }

    @Test
    @DisplayName("Создание инвентаря - успешное создание")
    void createInventory_ShouldCreateInventory_WhenValidData() {
        // Arrange
        String name = BLENDER_NAME;
        String description = "Кухонный блендер";

        when(inventoryRepository.existsByName(name)).thenReturn(false);
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);
        when(inventoryConverter.toDto(testInventory)).thenReturn(testInventoryDto);

        // Act
        InventoryDto result = inventoryService.createInventory(name, description);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(testInventoryDto);
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    @DisplayName("Создание инвентаря - инвентарь уже существует")
    void createInventory_ShouldThrowException_WhenInventoryExists() {
        // Arrange
        String description = "Электрическая духовка";

        when(inventoryRepository.existsByName(OVEN_NAME)).thenReturn(true);
        when(messageProvider.getMessage("inventory.already.exists", OVEN_NAME))
                .thenReturn("Инвентарь с названием Духовка уже существует");

        // Act & Assert
        assertThatThrownBy(() -> inventoryService.createInventory(OVEN_NAME, description))
                .isInstanceOf(EntityAlreadyExistsException.class)
                .hasMessage("Инвентарь с названием Духовка уже существует");

        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    @DisplayName("Создание инвентаря - пустое имя")
    void createInventory_ShouldThrowException_WhenNameIsEmpty() {
        // Arrange
        String name = "   ";
        String description = "Описание инвентаря";

        when(messageProvider.getMessage("inventory.name.empty"))
                .thenReturn("Название инвентаря не может быть пустым");

        // Act & Assert
        assertThatThrownBy(() -> inventoryService.createInventory(name, description))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Название инвентаря не может быть пустым");

        verify(inventoryRepository, never()).existsByName(anyString());
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    @DisplayName("Создание инвентаря - null имя")
    void createInventory_ShouldThrowException_WhenNameIsNull() {
        // Arrange
        String description = "Описание инвентаря";

        when(messageProvider.getMessage("inventory.name.empty"))
                .thenReturn("Название инвентаря не может быть пустым");

        // Act & Assert
        assertThatThrownBy(() -> inventoryService.createInventory(null, description))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Название инвентаря не может быть пустым");

        verify(inventoryRepository, never()).existsByName(anyString());
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    @DisplayName("Получение инвентаря по ID - инвентарь найден")
    void getInventoryById_ShouldReturnInventory_WhenInventoryExists() {
        // Arrange
        when(inventoryRepository.findById(EXISTING_INVENTORY_ID)).thenReturn(Optional.of(testInventory));
        when(inventoryConverter.toDto(testInventory)).thenReturn(testInventoryDto);

        // Act
        Optional<InventoryDto> result = inventoryService.getInventoryById(EXISTING_INVENTORY_ID);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testInventoryDto);
    }

    @Test
    @DisplayName("Получение инвентаря по ID с рецептами - инвентарь найден")
    void getInventoryByIdWithRecipes_ShouldReturnInventory_WhenInventoryExists() {
        // Arrange
        when(inventoryRepository.findById(EXISTING_INVENTORY_ID)).thenReturn(Optional.of(testInventory));
        when(inventoryConverter.toDto(testInventory)).thenReturn(testInventoryDto);

        // Act
        Optional<InventoryDto> result = inventoryService.getInventoryByIdWithRecipes(EXISTING_INVENTORY_ID);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testInventoryDto);
    }

    @Test
    @DisplayName("Получение инвентаря по ID - инвентарь не найден")
    void getInventoryById_ShouldReturnEmpty_WhenInventoryNotExists() {
        // Arrange
        when(inventoryRepository.findById(NON_EXISTING_INVENTORY_ID)).thenReturn(Optional.empty());

        // Act
        Optional<InventoryDto> result = inventoryService.getInventoryById(NON_EXISTING_INVENTORY_ID);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Поиск инвентаря по имени - с поисковым запросом")
    void getInventoryByNameContaining_ShouldReturnMatchingInventory_WhenSearchQueryProvided() {
        // Arrange
        String searchName = "духов";
        List<Inventory> inventoryList = List.of(testInventory);
        when(inventoryRepository.findByNameContainingIgnoreCase(searchName)).thenReturn(inventoryList);
        when(inventoryConverter.toDto(testInventory)).thenReturn(testInventoryDto);

        // Act
        List<InventoryDto> result = inventoryService.getInventoryByNameContaining(searchName);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testInventoryDto);
    }

    @Test
    @DisplayName("Поиск инвентаря по имени - без поискового запроса")
    void getInventoryByNameContaining_ShouldReturnAllInventory_WhenNoSearchQuery() {
        // Arrange
        List<Inventory> inventoryList = List.of(testInventory);
        when(inventoryRepository.findAllByOrderByNameAsc()).thenReturn(inventoryList);
        when(inventoryConverter.toDto(testInventory)).thenReturn(testInventoryDto);

        // Act
        List<InventoryDto> result = inventoryService.getInventoryByNameContaining(null);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testInventoryDto);
    }

    @Test
    @DisplayName("Получение всего инвентаря - успешно")
    void getAllInventory_ShouldReturnAllInventory() {
        // Arrange
        List<Inventory> inventoryList = List.of(testInventory);
        when(inventoryRepository.findAllByOrderByNameAsc()).thenReturn(inventoryList);
        when(inventoryConverter.toDto(testInventory)).thenReturn(testInventoryDto);

        // Act
        List<InventoryDto> result = inventoryService.getAllInventory();

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testInventoryDto);
    }

    @Test
    @DisplayName("Получение инвентаря по списку имен - успешно")
    void getInventoryByNames_ShouldReturnMatchingInventory() {
        // Arrange
        List<String> names = List.of(OVEN_NAME);
        List<Inventory> inventoryList = List.of(testInventory);
        when(inventoryRepository.findByNames(names)).thenReturn(inventoryList);
        when(inventoryConverter.toDto(testInventory)).thenReturn(testInventoryDto);

        // Act
        List<InventoryDto> result = inventoryService.getInventoryByNames(names);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testInventoryDto);
    }

    @Test
    @DisplayName("Получение инвентаря по ID рецепта - успешно")
    void getInventoryByRecipeId_ShouldReturnInventoryForRecipe() {
        // Arrange
        List<Inventory> inventoryList = List.of(testInventory);
        when(inventoryRepository.findByRecipeId(EXISTING_RECIPE_ID)).thenReturn(inventoryList);
        when(inventoryConverter.toDto(testInventory)).thenReturn(testInventoryDto);

        // Act
        List<InventoryDto> result = inventoryService.getInventoryByRecipeId(EXISTING_RECIPE_ID);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testInventoryDto);
    }

    @Test
    @DisplayName("Обновление инвентаря - успешно")
    void updateInventory_ShouldUpdateInventory_WhenInventoryExists() {
        // Arrange
        Inventory updatedInventory = new Inventory(OVEN_NAME, UPDATED_DESCRIPTION);
        updatedInventory.setId(EXISTING_INVENTORY_ID);
        InventoryDto updatedInventoryDto = new InventoryDto(EXISTING_INVENTORY_ID, OVEN_NAME,
                UPDATED_DESCRIPTION, LocalDateTime.now());

        when(inventoryRepository.findById(EXISTING_INVENTORY_ID)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(testInventory)).thenReturn(updatedInventory);
        when(inventoryConverter.toDto(updatedInventory)).thenReturn(updatedInventoryDto);

        // Act
        InventoryDto result = inventoryService.updateInventory(EXISTING_INVENTORY_ID, UPDATED_DESCRIPTION);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.description()).isEqualTo(UPDATED_DESCRIPTION);
        verify(inventoryRepository).save(testInventory);
    }

    @Test
    @DisplayName("Обновление инвентаря - инвентарь не найден")
    void updateInventory_ShouldThrowException_WhenInventoryNotFound() {
        // Arrange
        when(inventoryRepository.findById(NON_EXISTING_INVENTORY_ID)).thenReturn(Optional.empty());
        when(messageProvider.getMessage("inventory.not.found", NON_EXISTING_INVENTORY_ID))
                .thenReturn("Инвентарь не найден");

        // Act & Assert
        assertThatThrownBy(() -> inventoryService.updateInventory(NON_EXISTING_INVENTORY_ID, UPDATED_DESCRIPTION))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Инвентарь не найден");

        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    @DisplayName("Удаление инвентаря - успешно, когда не используется")
    void deleteInventory_ShouldDeleteInventory_WhenNotUsedInRecipes() {
        // Arrange
        when(inventoryRepository.findById(EXISTING_INVENTORY_ID)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.isUsedInRecipes(EXISTING_INVENTORY_ID)).thenReturn(false);

        // Act
        inventoryService.deleteInventory(EXISTING_INVENTORY_ID);

        // Assert
        verify(inventoryRepository).delete(testInventory);
    }

    @Test
    @DisplayName("Удаление инвентаря - ошибка, когда используется в рецептах")
    void deleteInventory_ShouldThrowException_WhenUsedInRecipes() {
        // Arrange
        when(inventoryRepository.findById(USED_INVENTORY_ID)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.isUsedInRecipes(USED_INVENTORY_ID)).thenReturn(true);
        when(messageProvider.getMessage("inventory.delete_used_error"))
                .thenReturn("Невозможно удалить инвентарь, так как он используется в рецептах");

        // Act & Assert
        assertThatThrownBy(() -> inventoryService.deleteInventory(USED_INVENTORY_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Невозможно удалить инвентарь, так как он используется в рецептах");

        verify(inventoryRepository, never()).delete(any(Inventory.class));
    }

    @Test
    @DisplayName("Удаление инвентаря - инвентарь не найден")
    void deleteInventory_ShouldThrowException_WhenInventoryNotFound() {
        // Arrange
        when(inventoryRepository.findById(NON_EXISTING_INVENTORY_ID)).thenReturn(Optional.empty());
        when(messageProvider.getMessage("inventory.not.found", NON_EXISTING_INVENTORY_ID))
                .thenReturn("Инвентарь не найден");

        // Act & Assert
        assertThatThrownBy(() -> inventoryService.deleteInventory(NON_EXISTING_INVENTORY_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Инвентарь не найден");

        verify(inventoryRepository, never()).delete(any(Inventory.class));
    }

    @Test
    @DisplayName("Проверка использования инвентаря в рецептах - используется")
    void isInventoryUsedInRecipes_ShouldReturnTrue_WhenInventoryUsed() {
        // Arrange
        when(inventoryRepository.isUsedInRecipes(USED_INVENTORY_ID)).thenReturn(true);

        // Act
        boolean result = inventoryService.isInventoryUsedInRecipes(USED_INVENTORY_ID);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Проверка использования инвентаря в рецептах - не используется")
    void isInventoryUsedInRecipes_ShouldReturnFalse_WhenInventoryNotUsed() {
        // Arrange
        when(inventoryRepository.isUsedInRecipes(EXISTING_INVENTORY_ID)).thenReturn(false);

        // Act
        boolean result = inventoryService.isInventoryUsedInRecipes(EXISTING_INVENTORY_ID);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Получение количества рецептов по инвентарю - успешно")
    void getRecipeCountByInventory_ShouldReturnCount() {
        // Arrange
        long expectedCount = 5L;
        when(inventoryRepository.countRecipesByInventoryId(EXISTING_INVENTORY_ID)).thenReturn(expectedCount);

        // Act
        long result = inventoryService.getRecipeCountByInventory(EXISTING_INVENTORY_ID);

        // Assert
        assertThat(result).isEqualTo(expectedCount);
    }

    @Test
    @DisplayName("Получение неиспользуемого инвентаря - успешно")
    void getUnusedInventory_ShouldReturnUnusedInventory() {
        // Arrange
        List<Inventory> unusedInventory = List.of(testInventory);
        when(inventoryRepository.findUnusedInventory()).thenReturn(unusedInventory);
        when(inventoryConverter.toDto(testInventory)).thenReturn(testInventoryDto);

        // Act
        List<InventoryDto> result = inventoryService.getUnusedInventory();

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testInventoryDto);
    }

    @Test
    @DisplayName("Получение инвентаря с информацией об использовании - успешно")
    void getInventoryWithUsage_ShouldReturnInventoryWithUsage() {
        // Arrange
        List<Inventory> inventoryList = List.of(testInventory);
        when(inventoryRepository.findAllByOrderByNameAsc()).thenReturn(inventoryList);
        when(inventoryConverter.toDto(testInventory)).thenReturn(testInventoryDto);
        when(inventoryRepository.countRecipesByInventoryId(EXISTING_INVENTORY_ID)).thenReturn(5L);
        when(inventoryConverter.toDtoWithUsageFromDto(testInventoryDto, 5L)).thenReturn(testInventoryWithUsageDto);

        // Act
        List<InventoryWithUsageDto> result = inventoryService.getInventoryWithUsage();

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testInventoryWithUsageDto);
    }

    @Test
    @DisplayName("Получение инвентаря с информацией об использовании опубликованных рецептов - пустой список")
    void getInventoryWithPublishedUsage_ShouldReturnEmptyList() {
        // Act
        List<InventoryWithUsageDto> result = inventoryService.getInventoryWithPublishedUsage();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Получение статуса использования инвентаря - успешно")
    void getInventoryUsageStatus_ShouldReturnUsageStatus() {
        // Arrange
        List<Long> inventoryIds = List.of(EXISTING_INVENTORY_ID, ANOTHER_INVENTORY_ID);
        when(inventoryRepository.isUsedInRecipes(EXISTING_INVENTORY_ID)).thenReturn(true);
        when(inventoryRepository.isUsedInRecipes(ANOTHER_INVENTORY_ID)).thenReturn(false);

        // Act
        Map<Long, Boolean> result = inventoryService.getInventoryUsageStatus(inventoryIds);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(EXISTING_INVENTORY_ID)).isTrue();
        assertThat(result.get(ANOTHER_INVENTORY_ID)).isFalse();
    }

    @Test
    @DisplayName("Получение инвентаря по ID для внутреннего использования - успешно")
    void getInventoryByIdsForInternalUse_ShouldReturnInventory_WhenIdsProvided() {
        // Arrange
        List<Long> inventoryIds = List.of(EXISTING_INVENTORY_ID, ANOTHER_INVENTORY_ID);
        List<Inventory> inventoryList = List.of(testInventory);
        when(inventoryRepository.findAllById(inventoryIds)).thenReturn(inventoryList);
        when(inventoryConverter.toDto(testInventory)).thenReturn(testInventoryDto);

        // Act
        List<InventoryDto> result = inventoryService.getInventoryByIdsForInternalUse(inventoryIds);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testInventoryDto);
    }

    @Test
    @DisplayName("Получение инвентаря по ID для внутреннего использования - пустой список при null")
    void getInventoryByIdsForInternalUse_ShouldReturnEmptyList_WhenIdsNull() {
        // Act
        List<InventoryDto> result = inventoryService.getInventoryByIdsForInternalUse(null);

        // Assert
        assertThat(result).isEmpty();
        verify(inventoryRepository, never()).findAllById(any());
    }

    @Test
    @DisplayName("Получение инвентаря по ID для внутреннего использования - пустой список при пустом списке")
    void getInventoryByIdsForInternalUse_ShouldReturnEmptyList_WhenIdsEmpty() {
        // Act
        List<InventoryDto> result = inventoryService.getInventoryByIdsForInternalUse(List.of());

        // Assert
        assertThat(result).isEmpty();
        verify(inventoryRepository, never()).findAllById(any());
    }

    @Test
    @DisplayName("Получение инвентаря по имени с рецептами - успешно")
    void getInventoryByNameContainingWithRecipes_ShouldReturnInventory() {
        // Arrange
        String searchName = "духов";
        List<Inventory> inventoryList = List.of(testInventory);
        when(inventoryRepository.findByNameContainingIgnoreCase(searchName)).thenReturn(inventoryList);
        when(inventoryConverter.toDto(testInventory)).thenReturn(testInventoryDto);

        // Act
        List<InventoryDto> result = inventoryService.getInventoryByNameContainingWithRecipes(searchName);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testInventoryDto);
    }
}