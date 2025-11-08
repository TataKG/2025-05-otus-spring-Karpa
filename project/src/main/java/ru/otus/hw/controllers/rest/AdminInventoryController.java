package ru.otus.hw.controllers.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.otus.hw.dto.ApiResponse;
import ru.otus.hw.dto.CategoryDto;
import ru.otus.hw.dto.InventoryDto;
import ru.otus.hw.exceptions.EntityAlreadyExistsException;
import ru.otus.hw.services.CategoryService;
import ru.otus.hw.services.InventoryService;
import ru.otus.hw.util.MessageProvider;

import java.util.List;

@RestController
@RequestMapping("/api/admin/inventory")
@RequiredArgsConstructor
public class AdminInventoryController {

    private final InventoryService inventoryService;
    private final MessageProvider messageProvider;

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryDto>> updateInventory(
            @PathVariable Long id,
            @RequestBody UpdateInventoryRequest request) {

        InventoryDto inventoryDto = inventoryService.updateInventory(id, request.description());
        return ResponseEntity.ok(
                ApiResponse.success(inventoryDto, messageProvider.getMessage("inventory.updated"))
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteInventory(@PathVariable Long id) {
        try {
            inventoryService.deleteInventory(id);
            return ResponseEntity.ok(
                    ApiResponse.success(null, messageProvider.getMessage("inventory.deleted"))
            );
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InventoryDto>>> getAllInventory() {
        try {
            System.out.println("Getting all inventory...");
            List<InventoryDto> inventory = inventoryService.getAllInventory();
            System.out.println("Found " + inventory.size() + " inventory items");
            return ResponseEntity.ok(ApiResponse.success(inventory));
        } catch (Exception e) {
            System.out.println("Error loading inventory: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Ошибка загрузки инвентаря: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/usage")
    public ResponseEntity<ApiResponse<InventoryUsageResponse>> getInventoryUsage(@PathVariable Long id) {
        boolean isUsed = inventoryService.isInventoryUsedInRecipes(id);
        long recipeCount = inventoryService.getRecipeCountByInventory(id);

        InventoryUsageResponse usage = new InventoryUsageResponse(isUsed, recipeCount);
        return ResponseEntity.ok(ApiResponse.success(usage));
    }

    @GetMapping("/unused")
    public ResponseEntity<ApiResponse<List<InventoryDto>>> getUnusedInventory() {
        List<InventoryDto> unusedInventory = inventoryService.getUnusedInventory();
        return ResponseEntity.ok(ApiResponse.success(unusedInventory));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InventoryDto>> createInventory(@RequestBody CreateInventoryRequest request) {
        try {
            System.out.println("Creating inventory: " + request.name() + ", " + request.description());

            if (request.name() == null || request.name().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Название инвентаря не может быть пустым"));
            }

            InventoryDto inventoryDto = inventoryService.createInventory(
                    request.name().trim(),
                    request.description() != null ? request.description().trim() : null
            );

            System.out.println("Inventory created successfully: " + inventoryDto.id());
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.success(inventoryDto, "Инвентарь успешно создан")
            );
        } catch (EntityAlreadyExistsException e) {
            System.out.println("Inventory already exists: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            System.out.println("Error creating inventory: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Ошибка при создании инвентаря: " + e.getMessage()));
        }
    }

    // Добавляем record для запроса создания
    public record CreateInventoryRequest(String name, String description) {}

    public record UpdateInventoryRequest(String description) {}
    public record InventoryUsageResponse(boolean isUsed, long recipeCount) {}
}
