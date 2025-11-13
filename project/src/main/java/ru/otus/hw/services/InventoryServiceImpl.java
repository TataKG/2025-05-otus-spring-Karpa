package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.converters.InventoryConverter;
import ru.otus.hw.dto.InventoryDto;
import ru.otus.hw.dto.InventoryWithUsageDto;
import ru.otus.hw.exceptions.EntityAlreadyExistsException;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Inventory;
import ru.otus.hw.repositories.InventoryRepository;
import ru.otus.hw.util.MessageProvider;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {
    private final InventoryRepository inventoryRepository;
    private final InventoryConverter inventoryConverter;
    private final MessageProvider messageProvider;

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDto> getInventoryByIdsForInternalUse(List<Long> inventoryIds) {
        try {
            if (inventoryIds == null || inventoryIds.isEmpty()) {
                return new ArrayList<>();
            }

            List<Inventory> inventoryList = new ArrayList<>();
            inventoryRepository.findAllById(inventoryIds).forEach(inventoryList::add);

            if (inventoryList.size() != inventoryIds.size()) {
                Set<Long> foundIds = inventoryList.stream()
                        .map(Inventory::getId)
                        .collect(Collectors.toSet());
                List<Long> missingIds = inventoryIds.stream()
                        .filter(id -> !foundIds.contains(id))
                        .collect(Collectors.toList());
            }

            List<InventoryDto> result = inventoryList.stream()
                    .map(inventoryConverter::toDto)
                    .collect(Collectors.toList());
            return result;
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    @Transactional
    public InventoryDto createInventory(String name, String description) {
        validateInventoryName(name);
        String trimmedName = name.trim();

        if (inventoryRepository.existsByName(trimmedName)) {
            throw new EntityAlreadyExistsException(
                    messageProvider.getMessage("inventory.already_exists", trimmedName)
            );
        }

        Inventory inventory = new Inventory(trimmedName, description != null ? description.trim() : null);
        Inventory savedInventory = inventoryRepository.save(inventory);
        return inventoryConverter.toDto(savedInventory);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InventoryDto> getInventoryById(Long id) {
        return inventoryRepository.findById(id)
                .map(inventoryConverter::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InventoryDto> getInventoryByIdWithRecipes(Long id) {
        return inventoryRepository.findById(id)
                .map(inventory -> {
                    if (inventory.getRecipes() != null) {
                        inventory.getRecipes().size();
                    }
                    return inventoryConverter.toDto(inventory);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDto> getInventoryByNameContaining(String name) {
        if (name == null || name.trim().isEmpty()) {
            return inventoryRepository.findAllByOrderByNameAsc().stream()
                    .map(inventoryConverter::toDto)
                    .collect(Collectors.toList());
        }

        return inventoryRepository.findByNameContainingIgnoreCase(name).stream()
                .map(inventoryConverter::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDto> getAllInventory() {
        try {
            List<Inventory> inventoryList = inventoryRepository.findAllByOrderByNameAsc();
            return inventoryList.stream()
                    .map(inventoryConverter::toDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDto> getAllInventoryWithRecipes() {
        return inventoryRepository.findAllWithRecipes().stream()
                .map(inventoryConverter::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDto> getInventoryByNames(List<String> names) {
        return inventoryRepository.findByNames(names).stream()
                .map(inventoryConverter::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDto> getInventoryByRecipeId(Long recipeId) {
        return inventoryRepository.findByRecipeId(recipeId).stream()
                .map(inventoryConverter::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public InventoryDto updateInventory(Long id, String description) {
        Inventory inventory = getInventoryEntity(id);
        inventory.setDescription(description);
        Inventory updatedInventory = inventoryRepository.save(inventory);
        return inventoryConverter.toDto(updatedInventory);
    }

    @Override
    @Transactional
    public void deleteInventory(Long id) {
        Inventory inventory = getInventoryEntity(id);

        if (inventoryRepository.isUsedInRecipes(id)) {
            throw new IllegalStateException(
                    messageProvider.getMessage("inventory.delete_used_error")
            );
        }

        inventoryRepository.delete(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isInventoryUsedInRecipes(Long inventoryId) {
        return inventoryRepository.isUsedInRecipes(inventoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getRecipeCountByInventory(Long inventoryId) {
        return inventoryRepository.countRecipesByInventoryId(inventoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDto> getUnusedInventory() {
        return inventoryRepository.findUnusedInventory().stream()
                .map(inventoryConverter::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryWithUsageDto> getInventoryWithUsage() {
        List<Object[]> results = inventoryRepository.findAllWithRecipeCount();

        return results.stream()
                .map(result -> {
                    Inventory inventory = (Inventory) result[0];
                    Long recipeCount = (Long) result[1];
                    return inventoryConverter.toDtoWithUsage(inventory, recipeCount);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryWithUsageDto> getInventoryWithPublishedUsage() {
        List<Object[]> results = inventoryRepository.findAllWithPublishedRecipeCount();

        return results.stream()
                .map(result -> {
                    Inventory inventory = (Inventory) result[0];
                    Long publishedRecipeCount = (Long) result[1];
                    return inventoryConverter.toDtoWithUsage(inventory, publishedRecipeCount);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Boolean> getInventoryUsageStatus(List<Long> inventoryIds) {
        return inventoryIds.stream()
                .collect(Collectors.toMap(
                        inventoryId -> inventoryId,
                        inventoryRepository::isUsedInRecipes
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDto> getInventoryByNameContainingWithRecipes(String name) {
        if (name == null || name.trim().isEmpty()) {
            return inventoryRepository.findAllWithRecipes().stream()
                    .map(inventoryConverter::toDto)
                    .collect(Collectors.toList());
        }

        return inventoryRepository.findByNameContainingIgnoreCaseWithRecipes(name).stream()
                .map(inventoryConverter::toDto)
                .collect(Collectors.toList());
    }

    private void validateInventoryName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    messageProvider.getMessage("inventory.name_empty")
            );
        }
    }

    private Inventory getInventoryEntity(Long id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("inventory.not_found", id)
                ));
    }
}