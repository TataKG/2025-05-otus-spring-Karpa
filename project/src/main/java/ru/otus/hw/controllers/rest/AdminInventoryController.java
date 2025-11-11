package ru.otus.hw.controllers.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.hw.dto.ApiResponse;
import ru.otus.hw.dto.InventoryDto;
import ru.otus.hw.exceptions.EntityAlreadyExistsException;
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
            boolean deleted = inventoryService.deleteInventory(id);
            if (deleted) {
                return ResponseEntity.ok(ApiResponse.success(null, messageProvider.getMessage("inventory.deleted")));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(messageProvider.getMessage("inventory.delete_used_error")));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(messageProvider.getMessage("inventory.delete_error") + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InventoryDto>>> getAllInventory() {
        try {
            List<InventoryDto> inventory = inventoryService.getAllInventory();
            return ResponseEntity.ok(ApiResponse.success(inventory));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(messageProvider.getMessage("inventory.load_error") + e.getMessage()));
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
            if (request.name() == null || request.name().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error(messageProvider.getMessage("inventory.name_empty")));
            }

            InventoryDto inventoryDto = inventoryService.createInventory(
                    request.name().trim(),
                    request.description() != null ? request.description().trim() : null
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.success(inventoryDto, messageProvider.getMessage("inventory.created"))
            );
        } catch (EntityAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(messageProvider.getMessage("inventory.create_error") + e.getMessage()));
        }
    }

    public record CreateInventoryRequest(String name, String description) {
    }

    public record UpdateInventoryRequest(String description) {
    }

    public record InventoryUsageResponse(boolean isUsed, long recipeCount) {
    }
}