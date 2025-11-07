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

    List<InventoryDto> getInventoryByRecipeId(Long recipeId);

    InventoryDto updateInventory(Long id, String description);

    void deleteInventory(Long id);

    boolean isInventoryUsedInRecipes(Long inventoryId);

    long getRecipeCountByInventory(Long inventoryId);

    List<InventoryDto> getUnusedInventory();

}
