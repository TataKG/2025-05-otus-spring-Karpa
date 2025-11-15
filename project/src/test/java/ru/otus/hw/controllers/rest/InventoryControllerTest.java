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
import ru.otus.hw.dto.InventoryDto;
import ru.otus.hw.exceptions.EntityAlreadyExistsException;
import ru.otus.hw.services.InventoryService;
import ru.otus.hw.utils.MessageProvider;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventoryController.class)
@Import(SecurityConfig.class)
class InventoryControllerTest {

    private static final Long EXISTING_INVENTORY_ID = 1L;
    private static final Long NON_EXISTING_INVENTORY_ID = 999L;
    private static final String INVENTORY_NAME = "Test Inventory";
    private static final String INVENTORY_DESCRIPTION = "Test inventory description";
    private static final String SEARCH_QUERY = "test";

    private static final String INVENTORY_CREATED_MESSAGE = "Инвентарь создан";
    private static final String INVENTORY_NOT_FOUND_MESSAGE = "Инвентарь не найден";
    private static final String INVENTORY_SEARCH_ERROR_MESSAGE = "Ошибка поиска инвентаря";
    private static final String INVENTORIES_LOAD_ERROR_MESSAGE = "Ошибка загрузки инвентаря";
    private static final String INVENTORY_NAMES_EMPTY_MESSAGE = "Список названий инвентаря пуст";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;

    @MockBean
    private MessageProvider messageProvider;

    private InventoryDto testInventoryDto;

    @BeforeEach
    void setUp() {
        testInventoryDto = new InventoryDto(
                EXISTING_INVENTORY_ID,
                INVENTORY_NAME,
                INVENTORY_DESCRIPTION,
                LocalDateTime.now()
        );

        // Mock message provider responses
        when(messageProvider.getMessage("inventory.created")).thenReturn(INVENTORY_CREATED_MESSAGE);
        when(messageProvider.getMessage("inventory.not_found", NON_EXISTING_INVENTORY_ID))
                .thenReturn(INVENTORY_NOT_FOUND_MESSAGE);
        when(messageProvider.getMessage("inventory.search_error")).thenReturn(INVENTORY_SEARCH_ERROR_MESSAGE);
        when(messageProvider.getMessage("inventories.load_error")).thenReturn(INVENTORIES_LOAD_ERROR_MESSAGE);
        when(messageProvider.getMessage("inventory.names_empty")).thenReturn(INVENTORY_NAMES_EMPTY_MESSAGE);
    }

    @Test
    @DisplayName("Получение инвентаря по ID - успешный случай")
    @WithMockUser
    void getInventoryById_ShouldReturnInventory_WhenInventoryExists() throws Exception {
        // Arrange
        when(inventoryService.getInventoryById(EXISTING_INVENTORY_ID))
                .thenReturn(java.util.Optional.of(testInventoryDto));

        // Act & Assert
        mockMvc.perform(get("/api/inventories/{id}", EXISTING_INVENTORY_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(EXISTING_INVENTORY_ID))
                .andExpect(jsonPath("$.data.name").value(INVENTORY_NAME))
                .andExpect(jsonPath("$.data.description").value(INVENTORY_DESCRIPTION));
    }

    @Test
    @DisplayName("Получение инвентаря по ID - инвентарь не найден")
    @WithMockUser
    void getInventoryById_ShouldReturnNotFound_WhenInventoryNotExists() throws Exception {
        // Arrange
        when(inventoryService.getInventoryById(NON_EXISTING_INVENTORY_ID))
                .thenReturn(java.util.Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/inventories/{id}", NON_EXISTING_INVENTORY_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(INVENTORY_NOT_FOUND_MESSAGE));
    }

    @Test
    @DisplayName("Поиск инвентаря по имени - успешный случай")
    @WithMockUser
    void searchInventoryByName_ShouldReturnInventory_WhenFound() throws Exception {
        // Arrange
        List<InventoryDto> inventoryList = List.of(testInventoryDto);
        when(inventoryService.getInventoryByNameContaining(SEARCH_QUERY))
                .thenReturn(inventoryList);

        // Act & Assert
        mockMvc.perform(get("/api/inventories/search")
                        .param("name", SEARCH_QUERY)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(EXISTING_INVENTORY_ID))
                .andExpect(jsonPath("$.data[0].name").value(INVENTORY_NAME));
    }

    @Test
    @DisplayName("Поиск инвентаря по имени - пустой результат")
    @WithMockUser
    void searchInventoryByName_ShouldReturnEmptyList_WhenNotFound() throws Exception {
        // Arrange
        when(inventoryService.getInventoryByNameContaining(SEARCH_QUERY))
                .thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/inventories/search")
                        .param("name", SEARCH_QUERY)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("Поиск инвентаря по имени - внутренняя ошибка сервера")
    @WithMockUser
    void searchInventoryByName_ShouldReturnInternalError_WhenServiceFails() throws Exception {
        // Arrange
        when(inventoryService.getInventoryByNameContaining(anyString()))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(get("/api/inventories/search")
                        .param("name", SEARCH_QUERY)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(INVENTORY_SEARCH_ERROR_MESSAGE));
    }

    @Test
    @DisplayName("Получение всего инвентаря - успешный случай")
    @WithMockUser
    void getAllInventory_ShouldReturnInventoryList() throws Exception {
        // Arrange
        List<InventoryDto> inventoryList = List.of(testInventoryDto);
        when(inventoryService.getAllInventory()).thenReturn(inventoryList);

        // Act & Assert
        mockMvc.perform(get("/api/inventories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(EXISTING_INVENTORY_ID))
                .andExpect(jsonPath("$.data[0].name").value(INVENTORY_NAME));
    }

    @Test
    @DisplayName("Получение всего инвентаря - пустой список")
    @WithMockUser
    void getAllInventory_ShouldReturnEmptyList_WhenNoInventory() throws Exception {
        // Arrange
        when(inventoryService.getAllInventory()).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/inventories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("Получение всего инвентаря - внутренняя ошибка сервера")
    @WithMockUser
    void getAllInventory_ShouldReturnInternalError_WhenServiceFails() throws Exception {
        // Arrange
        when(inventoryService.getAllInventory())
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(get("/api/inventories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(INVENTORIES_LOAD_ERROR_MESSAGE));
    }

    @Test
    @DisplayName("Получение инвентаря по именам - успешный случай")
    @WithMockUser
    void getInventoryByNames_ShouldReturnInventory_WhenValidRequest() throws Exception {
        // Arrange
        String requestBody = "[\"Inventory1\", \"Inventory2\"]";
        List<InventoryDto> inventoryList = List.of(testInventoryDto);
        when(inventoryService.getInventoryByNames(anyList()))
                .thenReturn(inventoryList);

        // Act & Assert
        mockMvc.perform(post("/api/inventories/by-names")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(EXISTING_INVENTORY_ID))
                .andExpect(jsonPath("$.data[0].name").value(INVENTORY_NAME));
    }

    @Test
    @DisplayName("Получение инвентаря по именам - пустой список имен")
    @WithMockUser
    void getInventoryByNames_ShouldReturnBadRequest_WhenNamesEmpty() throws Exception {
        // Arrange
        String requestBody = "[]";

        // Act & Assert
        mockMvc.perform(post("/api/inventories/by-names")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(INVENTORY_NAMES_EMPTY_MESSAGE));
    }

    @Test
    @DisplayName("Получение инвентаря по именам - неверный запрос")
    @WithMockUser
    void getInventoryByNames_ShouldReturnBadRequest_WhenInvalidRequest() throws Exception {
        // Arrange
        String errorMessage = "Неверный формат запроса";
        when(inventoryService.getInventoryByNames(anyList()))
                .thenThrow(new IllegalArgumentException(errorMessage));

        // Act & Assert
        mockMvc.perform(post("/api/inventories/by-names")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[\"ValidName\"]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(errorMessage));
    }

    @Test
    @DisplayName("Получение инвентаря по именам - внутренняя ошибка сервера")
    @WithMockUser
    void getInventoryByNames_ShouldReturnInternalError_WhenServiceFails() throws Exception {
        // Arrange
        when(inventoryService.getInventoryByNames(anyList()))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(post("/api/inventories/by-names")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[\"Inventory1\"]"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(INVENTORIES_LOAD_ERROR_MESSAGE));
    }

}