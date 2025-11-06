package ru.otus.hw.services;

import ru.otus.hw.dto.InventoryDto;

import java.util.List;
import java.util.Optional;

public interface InventoryService {
    InventoryDto createInventory(String name, String description);

    Optional<InventoryDto> getInventoryById(Long id);

    List<InventoryDto> getInventoryByNameContaining(String name);

    List<InventoryDto> getAllInventory();

    List<InventoryDto> getInventoryByNames(List<String> names);
}
