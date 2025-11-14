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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {
    private final InventoryRepository inventoryRepository;
    private final InventoryConverter inventoryConverter;
    private final MessageProvider messageProvider;

    @Override
    public List<InventoryDto> getInventoryByIdsForInternalUse(List<Long> inventoryIds) {
        if (inventoryIds == null || inventoryIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Inventory> inventoryList = new ArrayList<>();
        inventoryRepository.findAllById(inventoryIds).forEach(inventoryList::add);

        return inventoryList.stream()
                .map(inventoryConverter::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public InventoryDto createInventory(String name, String description) {
        validateInventoryName(name);
        String trimmedName = name.trim();

        if (inventoryRepository.existsByName(trimmedName)) {
            throw new EntityAlreadyExistsException(
                    messageProvider.getMessage("inventory.already.exists", trimmedName)
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
    public Optional<InventoryDto> getInventoryByIdWithRecipes(Long id) {
        return inventoryRepository.findById(id)
                .map(inventoryConverter::toDto);
    }

    @Override
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
    public List<InventoryDto> getAllInventory() {
        return inventoryRepository.findAllByOrderByNameAsc().stream()
                .map(inventoryConverter::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryDto> getAllInventoryWithRecipes() {
        return getAllInventory();
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
    public List<InventoryWithUsageDto> getInventoryWithUsage() {
        return getAllInventory().stream()
                .map(inventoryDto -> {
                    long recipeCount = inventoryRepository.countRecipesByInventoryId(inventoryDto.id());
                    return inventoryConverter.toDtoWithUsageFromDto(inventoryDto, recipeCount);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryWithUsageDto> getInventoryWithPublishedUsage() {
        return new ArrayList<>();
    }

    @Override
    public Map<Long, Boolean> getInventoryUsageStatus(List<Long> inventoryIds) {
        return inventoryIds.stream()
                .collect(Collectors.toMap(
                        inventoryId -> inventoryId,
                        inventoryRepository::isUsedInRecipes
                ));
    }

    @Override
    public List<InventoryDto> getInventoryByNameContainingWithRecipes(String name) {
        return getInventoryByNameContaining(name);
    }

    private void validateInventoryName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    messageProvider.getMessage("inventory.name.empty")
            );
        }
    }

    private Inventory getInventoryEntity(Long id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("inventory.not.found", id)
                ));
    }
}