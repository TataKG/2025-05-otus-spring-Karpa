package ru.otus.hw.controllers.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.hw.dto.ApiResponse;
import ru.otus.hw.dto.InventoryDto;
import ru.otus.hw.exceptions.EntityAlreadyExistsException;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.InventoryService;
import ru.otus.hw.utils.MessageProvider;

import java.util.List;

@RestController
@RequestMapping("/api/inventories")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final MessageProvider messageProvider;

    @PostMapping
    public ResponseEntity<ApiResponse<InventoryDto>> createInventory(@RequestBody CreateInventoryRequest request) {
        try {
            InventoryDto inventoryDto = inventoryService.createInventory(
                    request.name(),
                    request.description()
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
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryDto>> getInventoryById(@PathVariable Long id) {
        try {
            InventoryDto inventoryDto = inventoryService.getInventoryById(id)
                    .orElseThrow(() -> new EntityNotFoundException(
                            messageProvider.getMessage("inventory.not_found", id)
                    ));
            return ResponseEntity.ok(ApiResponse.success(inventoryDto));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<InventoryDto>>> searchInventoryByName(
            @RequestParam String name) {
        try {
            List<InventoryDto> inventory = inventoryService.getInventoryByNameContaining(name);
            return ResponseEntity.ok(ApiResponse.success(inventory));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(messageProvider.getMessage("inventory.search_error")));
        }
    }

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

    @PostMapping("/by-names")
    public ResponseEntity<ApiResponse<List<InventoryDto>>> getInventoryByNames(
            @RequestBody List<String> names) {
        try {
            if (names == null || names.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error(messageProvider.getMessage("inventory.names_empty")));
            }
            List<InventoryDto> inventory = inventoryService.getInventoryByNames(names);
            return ResponseEntity.ok(ApiResponse.success(inventory));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(messageProvider.getMessage("inventories.load_error")));
        }
    }

    public record CreateInventoryRequest(String name, String description) {
    }
}