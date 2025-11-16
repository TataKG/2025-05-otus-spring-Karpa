package ru.otus.hw.controllers.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
import ru.otus.hw.dto.InventoryWithUsageDto;
import ru.otus.hw.exceptions.EntityAlreadyExistsException;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.InventoryService;
import ru.otus.hw.utils.MessageProvider;

import java.util.List;

@RestController
@RequestMapping("/api/admin/inventory")
@RequiredArgsConstructor
public class AdminInventoryController {

    private final InventoryService inventoryService;
    private final MessageProvider messageProvider;

    @GetMapping
    public ResponseEntity<ApiResponse<List<InventoryDto>>> getAllInventory() {
        try {
            List<InventoryDto> inventory = inventoryService.getAllInventory();
            return ResponseEntity.ok(ApiResponse.success(inventory));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(messageProvider.getMessage("inventories.load_error")));
        }
    }

    @GetMapping("/with-usage")
    public ResponseEntity<ApiResponse<List<InventoryWithUsageDto>>> getInventoryWithUsage() {
        try {
            List<InventoryWithUsageDto> inventory = inventoryService.getInventoryWithUsage();
            return ResponseEntity.ok(ApiResponse.success(inventory));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(messageProvider.getMessage("inventories.load_error")));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InventoryDto>> createInventory(@RequestBody @Valid CreateInventoryRequest request) {
        try {
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
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryDto>> updateInventory(
            @PathVariable Long id,
            @RequestBody @Valid UpdateInventoryRequest request) {
        try {
            InventoryDto inventoryDto = inventoryService.updateInventory(id, request.description());
            return ResponseEntity.ok(
                    ApiResponse.success(inventoryDto, messageProvider.getMessage("inventory.updated"))
            );
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteInventory(@PathVariable Long id) {
        try {
            inventoryService.deleteInventory(id);
            return ResponseEntity.ok(ApiResponse.success(null, messageProvider.getMessage("inventory.deleted")));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}/usage")
    public ResponseEntity<ApiResponse<InventoryUsageResponse>> getInventoryUsage(@PathVariable Long id) {
        try {
            boolean isUsed = inventoryService.isInventoryUsedInRecipes(id);
            long recipeCount = inventoryService.getRecipeCountByInventory(id);
            InventoryUsageResponse usage = new InventoryUsageResponse(isUsed, recipeCount);
            return ResponseEntity.ok(ApiResponse.success(usage));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(messageProvider.getMessage("inventory.usage_error")));
        }
    }

    @GetMapping("/unused")
    public ResponseEntity<ApiResponse<List<InventoryDto>>> getUnusedInventory() {
        try {
            List<InventoryDto> unusedInventory = inventoryService.getUnusedInventory();
            return ResponseEntity.ok(ApiResponse.success(unusedInventory));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(messageProvider.getMessage("inventories.load_error")));
        }
    }

    @GetMapping("/with-published-usage")
    public ResponseEntity<ApiResponse<List<InventoryWithUsageDto>>> getInventoryWithPublishedUsage() {
        try {
            List<InventoryWithUsageDto> inventory = inventoryService.getInventoryWithPublishedUsage();
            return ResponseEntity.ok(ApiResponse.success(inventory));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(messageProvider.getMessage("inventories.load_error")));
        }
    }

    public record CreateInventoryRequest(
            @NotBlank(message = "{inventory.name.not.blank}")
            @Size(min = 2, max = 150, message = "{inventory.name.size}")
            @Pattern(regexp = "^[a-zA-Zа-яА-Я0-9\\s\\-]+$", message = "{inventory.name.pattern}")
            String name,

            @NotBlank(message = "{inventory.description.not.blank}")
            @Size(max = 255, message = "{inventory.description.size}")
            String description
    ) {
    }

    public record UpdateInventoryRequest(
            @NotBlank(message = "{inventory.description.not.blank}")
            @Size(max = 255, message = "{inventory.description.size}")
            String description) {
    }

    public record InventoryUsageResponse(boolean isUsed, long recipeCount) {
    }
}