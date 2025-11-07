package ru.otus.hw.controllers.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.otus.hw.dto.ApiResponse;
import ru.otus.hw.dto.CategoryDto;
import ru.otus.hw.dto.InventoryDto;
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

    public record UpdateInventoryRequest(String description) {}
    public record InventoryUsageResponse(boolean isUsed, long recipeCount) {}
}
