package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {
    private final InventoryRepository inventoryRepository;
    private final InventoryConverter inventoryConverter;
    private final MessageProvider messageProvider;

    @Override
    public List<InventoryDto> getInventoryByIdsForInternalUse(List<Long> inventoryIds) {
        try {
            System.out.println("Getting inventory by IDs: " + inventoryIds);

            if (inventoryIds == null || inventoryIds.isEmpty()) {
                System.out.println("No inventory IDs provided");
                return new ArrayList<>();
            }

            // Исправление: конвертируем Iterable в List
            Iterable<Inventory> inventoryIterable = inventoryRepository.findAllById(inventoryIds);
            List<Inventory> inventoryList = new ArrayList<>();
            inventoryIterable.forEach(inventoryList::add);

            System.out.println("Found " + inventoryList.size() + " inventory items");

            if (inventoryList.size() != inventoryIds.size()) {
                System.err.println("Warning: requested " + inventoryIds.size() + " items, but found " + inventoryList.size());

                // Найдем какие ID не найдены
                Set<Long> foundIds = inventoryList.stream()
                        .map(Inventory::getId)
                        .collect(Collectors.toSet());
                List<Long> missingIds = inventoryIds.stream()
                        .filter(id -> !foundIds.contains(id))
                        .collect(Collectors.toList());
                System.err.println("Missing inventory IDs: " + missingIds);
            }

            List<InventoryDto> result = inventoryList.stream()
                    .map(inventoryConverter::toDto)
                    .collect(Collectors.toList());

            System.out.println("Inventory conversion completed, returning " + result.size() + " items");
            return result;
        } catch (Exception e) {
            System.err.println("Error in getInventoryByIdsForInternalUse: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
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
    public Optional<InventoryDto> getInventoryById(Long id) {
        return inventoryRepository.findById(id)
                .map(inventoryConverter::toDto);
    }

    @Override
    public List<InventoryDto> getInventoryByNameContaining(String name) {
        return inventoryRepository.findByNameContainingIgnoreCase(name).stream()
                .map(inventoryConverter::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryDto> getAllInventory() {
        try {
            log.info("Getting all inventory ordered by name");
            List<Inventory> inventoryList = inventoryRepository.findAllByOrderByNameAsc();
            log.info("Found {} inventory items", inventoryList.size());

            return inventoryList.stream()
                    .map(inventoryConverter::toDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error in getAllInventory", e);
            throw e;
        }
    }

    @Override
    public List<InventoryDto> getInventoryByNames(List<String> names) {
        return inventoryRepository.findByNames(names).stream()
                .map(inventoryConverter::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryDto> getInventoryByRecipeId(Long recipeId) {
        return inventoryRepository.findByRecipeId(recipeId).stream()
                .map(inventoryConverter::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public InventoryDto updateInventory(Long id, String description) {
        Inventory inventory = getInventoryEntity(id);
        inventory.setDescription(description);
        Inventory updatedInventory = inventoryRepository.save(inventory);
        return inventoryConverter.toDto(updatedInventory);
    }

    @Override
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
    public boolean isInventoryUsedInRecipes(Long inventoryId) {
        return inventoryRepository.isUsedInRecipes(inventoryId);
    }

    @Override
    public long getRecipeCountByInventory(Long inventoryId) {
        return inventoryRepository.countRecipesByInventoryId(inventoryId);
    }

    @Override
    public List<InventoryDto> getUnusedInventory() {
        return inventoryRepository.findUnusedInventory().stream()
                .map(inventoryConverter::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryWithUsageDto> getInventoryWithUsage() {
        return inventoryRepository.findAllByOrderByNameAsc().stream()
                .map(inventory -> {
                    long recipeCount = inventoryRepository.countRecipesByInventoryId(inventory.getId());
                    return inventoryConverter.toDtoWithUsage(inventory, recipeCount);
                })
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