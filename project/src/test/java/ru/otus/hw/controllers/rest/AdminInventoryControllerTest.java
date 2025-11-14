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
import ru.otus.hw.dto.InventoryDto;
import ru.otus.hw.dto.InventoryWithUsageDto;
import ru.otus.hw.exceptions.EntityAlreadyExistsException;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.InventoryService;
import ru.otus.hw.util.MessageProvider;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты контроллера администратора для работы с инвентарем")
class AdminInventoryControllerTest {

    private static final Long EXISTING_INVENTORY_ID = 1L;
    private static final Long NON_EXISTING_INVENTORY_ID = 999L;
    private static final Long USED_INVENTORY_ID = 2L;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private MessageProvider messageProvider;

    @InjectMocks
    private AdminInventoryController adminInventoryController;

    @BeforeEach
    void setUp() {
        UserDetails adminUser = new User("admin", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(adminUser, null, adminUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("Получение всего инвентаря - успешное выполнение")
    void getAllInventory_Success() {
        // Arrange
        InventoryDto inventoryDto = new InventoryDto(
                EXISTING_INVENTORY_ID,
                "Духовка",
                "Электрическая духовка",
                LocalDateTime.now()
        );
        List<InventoryDto> inventory = List.of(inventoryDto);
        when(inventoryService.getAllInventory()).thenReturn(inventory);

        // Act
        ResponseEntity<ApiResponse<List<InventoryDto>>> response = adminInventoryController.getAllInventory();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data()).isEqualTo(inventory);
        verify(inventoryService).getAllInventory();
    }

    @Test
    @DisplayName("Получение всего инвентаря - внутренняя ошибка сервера")
    void getAllInventory_InternalServerError() {
        // Arrange
        when(inventoryService.getAllInventory()).thenThrow(new RuntimeException("Ошибка базы данных"));
        when(messageProvider.getMessage("inventories.load_error")).thenReturn("Ошибка загрузки инвентаря");

        // Act
        ResponseEntity<ApiResponse<List<InventoryDto>>> response = adminInventoryController.getAllInventory();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().message()).isEqualTo("Ошибка загрузки инвентаря");
        verify(inventoryService).getAllInventory();
    }

    @Test
    @DisplayName("Получение инвентаря с информацией об использовании - успешное выполнение")
    void getInventoryWithUsage_Success() {
        // Arrange
        InventoryWithUsageDto inventoryWithUsage = new InventoryWithUsageDto(
                EXISTING_INVENTORY_ID,
                "Блендер",
                "Кухонный блендер",
                LocalDateTime.now(),
                true,
                3L
        );
        List<InventoryWithUsageDto> inventory = List.of(inventoryWithUsage);
        when(inventoryService.getInventoryWithUsage()).thenReturn(inventory);

        // Act
        ResponseEntity<ApiResponse<List<InventoryWithUsageDto>>> response = adminInventoryController.getInventoryWithUsage();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data()).isEqualTo(inventory);
        verify(inventoryService).getInventoryWithUsage();
    }

    @Test
    @DisplayName("Создание инвентаря - успешное создание")
    void createInventory_Success() {
        // Arrange
        AdminInventoryController.CreateInventoryRequest request =
                new AdminInventoryController.CreateInventoryRequest("Миксер", "Кухонный миксер");
        InventoryDto inventoryDto = new InventoryDto(
                EXISTING_INVENTORY_ID,
                "Миксер",
                "Кухонный миксер",
                LocalDateTime.now()
        );

        when(inventoryService.createInventory("Миксер", "Кухонный миксер")).thenReturn(inventoryDto);
        when(messageProvider.getMessage("inventory.created")).thenReturn("Инвентарь успешно создан");

        // Act
        ResponseEntity<ApiResponse<InventoryDto>> response = adminInventoryController.createInventory(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data()).isEqualTo(inventoryDto);
        assertThat(response.getBody().message()).isEqualTo("Инвентарь успешно создан");
        verify(inventoryService).createInventory("Миксер", "Кухонный миксер");
    }

    @Test
    @DisplayName("Создание инвентаря - инвентарь уже существует")
    void createInventory_EntityAlreadyExists() {
        // Arrange
        AdminInventoryController.CreateInventoryRequest request =
                new AdminInventoryController.CreateInventoryRequest("Духовка", "Электрическая духовка");
        when(inventoryService.createInventory("Духовка", "Электрическая духовка"))
                .thenThrow(new EntityAlreadyExistsException("Инвентарь уже существует"));

        // Act
        ResponseEntity<ApiResponse<InventoryDto>> response = adminInventoryController.createInventory(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().message()).isEqualTo("Инвентарь уже существует");
    }

    @Test
    @DisplayName("Создание инвентаря - неверные аргументы")
    void createInventory_IllegalArgumentException() {
        // Arrange
        AdminInventoryController.CreateInventoryRequest request =
                new AdminInventoryController.CreateInventoryRequest("", "Описание");
        when(inventoryService.createInventory("", "Описание"))
                .thenThrow(new IllegalArgumentException("Название инвентаря не может быть пустым"));

        // Act
        ResponseEntity<ApiResponse<InventoryDto>> response = adminInventoryController.createInventory(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().message()).isEqualTo("Название инвентаря не может быть пустым");
    }

    @Test
    @DisplayName("Обновление инвентаря - успешное обновление")
    void updateInventory_Success() {
        // Arrange
        AdminInventoryController.UpdateInventoryRequest request =
                new AdminInventoryController.UpdateInventoryRequest("Обновленное описание духовки");
        InventoryDto inventoryDto = new InventoryDto(
                EXISTING_INVENTORY_ID,
                "Духовка",
                "Обновленное описание духовки",
                LocalDateTime.now()
        );

        when(inventoryService.updateInventory(EXISTING_INVENTORY_ID, "Обновленное описание духовки")).thenReturn(inventoryDto);
        when(messageProvider.getMessage("inventory.updated")).thenReturn("Инвентарь успешно обновлен");

        // Act
        ResponseEntity<ApiResponse<InventoryDto>> response = adminInventoryController.updateInventory(EXISTING_INVENTORY_ID, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data()).isEqualTo(inventoryDto);
        assertThat(response.getBody().message()).isEqualTo("Инвентарь успешно обновлен");
        verify(inventoryService).updateInventory(EXISTING_INVENTORY_ID, "Обновленное описание духовки");
    }

    @Test
    @DisplayName("Обновление инвентаря - инвентарь не найден")
    void updateInventory_EntityNotFound() {
        // Arrange
        AdminInventoryController.UpdateInventoryRequest request =
                new AdminInventoryController.UpdateInventoryRequest("Новое описание");
        when(inventoryService.updateInventory(NON_EXISTING_INVENTORY_ID, "Новое описание"))
                .thenThrow(new EntityNotFoundException("Инвентарь не найден"));

        // Act
        ResponseEntity<ApiResponse<InventoryDto>> response = adminInventoryController.updateInventory(NON_EXISTING_INVENTORY_ID, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().message()).isEqualTo("Инвентарь не найден");
    }

    @Test
    @DisplayName("Удаление инвентаря - успешное удаление")
    void deleteInventory_Success() {
        // Arrange
        when(messageProvider.getMessage("inventory.deleted")).thenReturn("Инвентарь успешно удален");

        // Act
        ResponseEntity<ApiResponse<Void>> response = adminInventoryController.deleteInventory(EXISTING_INVENTORY_ID);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().message()).isEqualTo("Инвентарь успешно удален");
        verify(inventoryService).deleteInventory(EXISTING_INVENTORY_ID);
    }

    @Test
    @DisplayName("Удаление инвентаря - инвентарь не найден")
    void deleteInventory_EntityNotFound() {
        // Arrange
        doThrow(new EntityNotFoundException("Инвентарь не найден"))
                .when(inventoryService).deleteInventory(NON_EXISTING_INVENTORY_ID);

        // Act
        ResponseEntity<ApiResponse<Void>> response = adminInventoryController.deleteInventory(NON_EXISTING_INVENTORY_ID);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().message()).isEqualTo("Инвентарь не найден");
    }

    @Test
    @DisplayName("Удаление инвентаря - конфликт при удалении используемого инвентаря")
    void deleteInventory_IllegalStateException() {
        // Arrange
        doThrow(new IllegalStateException("Невозможно удалить инвентарь, так как он используется в рецептах"))
                .when(inventoryService).deleteInventory(USED_INVENTORY_ID);

        // Act
        ResponseEntity<ApiResponse<Void>> response = adminInventoryController.deleteInventory(USED_INVENTORY_ID);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().message()).isEqualTo("Невозможно удалить инвентарь, так как он используется в рецептах");
    }

    @Test
    @DisplayName("Получение информации об использовании инвентаря - успешное выполнение")
    void getInventoryUsage_Success() {
        // Arrange
        when(inventoryService.isInventoryUsedInRecipes(EXISTING_INVENTORY_ID)).thenReturn(true);
        when(inventoryService.getRecipeCountByInventory(EXISTING_INVENTORY_ID)).thenReturn(3L);

        // Act
        ResponseEntity<ApiResponse<AdminInventoryController.InventoryUsageResponse>> response =
                adminInventoryController.getInventoryUsage(EXISTING_INVENTORY_ID);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data().isUsed()).isTrue();
        assertThat(response.getBody().data().recipeCount()).isEqualTo(3L);
        verify(inventoryService).isInventoryUsedInRecipes(EXISTING_INVENTORY_ID);
        verify(inventoryService).getRecipeCountByInventory(EXISTING_INVENTORY_ID);
    }

    @Test
    @DisplayName("Получение информации об использовании инвентаря - инвентарь не найден")
    void getInventoryUsage_EntityNotFound() {
        // Arrange
        when(inventoryService.isInventoryUsedInRecipes(NON_EXISTING_INVENTORY_ID))
                .thenThrow(new EntityNotFoundException("Инвентарь не найден"));

        // Act
        ResponseEntity<ApiResponse<AdminInventoryController.InventoryUsageResponse>> response =
                adminInventoryController.getInventoryUsage(NON_EXISTING_INVENTORY_ID);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().message()).isEqualTo("Инвентарь не найден");
    }

    @Test
    @DisplayName("Получение неиспользуемого инвентаря - успешное выполнение")
    void getUnusedInventory_Success() {
        // Arrange
        InventoryDto inventoryDto = new InventoryDto(
                5L,
                "Новый инвентарь",
                "Никогда не использовался",
                LocalDateTime.now()
        );
        List<InventoryDto> unusedInventory = List.of(inventoryDto);
        when(inventoryService.getUnusedInventory()).thenReturn(unusedInventory);

        // Act
        ResponseEntity<ApiResponse<List<InventoryDto>>> response = adminInventoryController.getUnusedInventory();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data()).isEqualTo(unusedInventory);
        verify(inventoryService).getUnusedInventory();
    }

    @Test
    @DisplayName("Получение инвентаря с информацией об использовании в опубликованных рецептах - успешное выполнение")
    void getInventoryWithPublishedUsage_Success() {
        // Arrange
        InventoryWithUsageDto inventoryWithUsage = new InventoryWithUsageDto(
                EXISTING_INVENTORY_ID,
                "Блендер",
                "Кухонный блендер",
                LocalDateTime.now(),
                true,
                2L
        );
        List<InventoryWithUsageDto> inventory = List.of(inventoryWithUsage);
        when(inventoryService.getInventoryWithPublishedUsage()).thenReturn(inventory);

        // Act
        ResponseEntity<ApiResponse<List<InventoryWithUsageDto>>> response = adminInventoryController.getInventoryWithPublishedUsage();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data()).isEqualTo(inventory);
        verify(inventoryService).getInventoryWithPublishedUsage();
    }
}