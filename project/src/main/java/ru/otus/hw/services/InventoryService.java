package ru.otus.hw.services;

import ru.otus.hw.dto.InventoryDto;
import ru.otus.hw.dto.InventoryWithUsageDto;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface InventoryService {
    InventoryDto createInventory(String name, String description);

    Optional<InventoryDto> getInventoryById(Long id);

    List<InventoryDto> getInventoryByNameContaining(String name);

    List<InventoryDto> getAllInventory();

    List<InventoryDto> getInventoryByNames(List<String> names);

    List<InventoryDto> getInventoryByRecipeId(Long recipeId);

    List<InventoryDto> getInventoryByIdsForInternalUse(List<Long> ids); // переименовать!

    InventoryDto updateInventory(Long id, String description);

    void deleteInventory(Long id);

    boolean isInventoryUsedInRecipes(Long inventoryId);

    long getRecipeCountByInventory(Long inventoryId);

    List<InventoryDto> getUnusedInventory();

    List<InventoryWithUsageDto> getInventoryWithUsage();

    List<InventoryDto> getInventoryByNameContainingWithRecipes(String name);

    Map<Long, Boolean> getInventoryUsageStatus(List<Long> inventoryIds);

    List<InventoryWithUsageDto> getInventoryWithPublishedUsage();

    Optional<InventoryDto> getInventoryByIdWithRecipes(Long id);

    List<InventoryDto> getAllInventoryWithRecipes();
}