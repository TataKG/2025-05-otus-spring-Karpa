package ru.otus.hw.controllers.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.otus.hw.dto.ApiResponse;
import ru.otus.hw.dto.InventoryDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.InventoryService;
import ru.otus.hw.util.MessageProvider;

import java.util.List;

//@RestController
//@RequestMapping("/api/inventories")
public class InventoryController {

//    private final InventoryService inventoryService;
//    private final MessageProvider messageProvider;
//
//    public InventoryController(InventoryService inventoryService, MessageProvider messageProvider) {
//        this.inventoryService = inventoryService;
//        this.messageProvider = messageProvider;
//    }
//
//    @PostMapping
//    public ResponseEntity<ApiResponse<InventoryDto>> createInventory(@RequestBody CreateInventoryRequest request) {
//        InventoryDto inventoryDto = inventoryService.createInventory(request.name(), request.description());
//        return ResponseEntity.status(HttpStatus.CREATED).body(
//                ApiResponse.success(inventoryDto, messageProvider.getMessage("inventory.created"))
//        );
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<ApiResponse<InventoryDto>> getInventoryById(@PathVariable Long id) {
//        InventoryDto inventoryDto = inventoryService.getInventoryById(id)
//                .orElseThrow(() -> new EntityNotFoundException(
//                        messageProvider.getMessage("inventory.not_found", id)
//                ));
//        return ResponseEntity.ok(ApiResponse.success(inventoryDto));
//    }
//
//    @GetMapping("/search")
//    public ResponseEntity<ApiResponse<List<InventoryDto>>> searchInventoryByName(
//            @RequestParam String name) {
//        List<InventoryDto> inventory = inventoryService.getInventoryByNameContaining(name);
//        return ResponseEntity.ok(ApiResponse.success(inventory));
//    }
//
//    @GetMapping
//    public ResponseEntity<ApiResponse<List<InventoryDto>>> getAllInventory() {
//        List<InventoryDto> inventory = inventoryService.getAllInventory();
//        return ResponseEntity.ok(ApiResponse.success(inventory));
//    }
//
//    @PostMapping("/by-names")
//    public ResponseEntity<ApiResponse<List<InventoryDto>>> getInventoryByNames(
//            @RequestBody List<String> names) {
//        List<InventoryDto> inventory = inventoryService.getInventoryByNames(names);
//        return ResponseEntity.ok(ApiResponse.success(inventory));
//    }
//
//    public record CreateInventoryRequest(String name, String description) {}
}