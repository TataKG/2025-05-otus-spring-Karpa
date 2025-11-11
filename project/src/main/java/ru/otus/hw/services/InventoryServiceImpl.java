package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.converters.InventoryConverter;
import ru.otus.hw.dto.InventoryDto;
import ru.otus.hw.exceptions.EntityAlreadyExistsException;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.exceptions.ValidationException;
import ru.otus.hw.models.Inventory;
import ru.otus.hw.repositories.InventoryRepository;
import ru.otus.hw.util.MessageProvider;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Transactional
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryConverter inventoryConverter;
    private final MessageProvider messageProvider;

    @Override
    public InventoryDto createInventory(String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException(messageProvider.getMessage("inventory.name.required"));
        }

        String trimmedName = name.trim();

        Optional<Inventory> existingInventory = inventoryRepository.findByName(trimmedName);
        if (existingInventory.isPresent()) {
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
    public List<InventoryDto> getInventoryByNameContaining(String name) {
        return inventoryRepository.findByNameContainingIgnoreCase(name).stream()
                .map(inventoryConverter::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryDto> getAllInventory() {
        return StreamSupport.stream(inventoryRepository.findAll().spliterator(), false)
                .map(inventoryConverter::toDto)
                .collect(Collectors.toList());
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
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("inventory.not_found", id)
                ));

        inventory.setDescription(description);
        Inventory updatedInventory = inventoryRepository.save(inventory);
        return inventoryConverter.toDto(updatedInventory);
    }

    @Override
    @Transactional
    public boolean deleteInventory(Long id) {
        try {
            boolean isUsed = isInventoryUsedInRecipes(id);
            if (isUsed) {
                return false;
            }

            inventoryRepository.deleteById(id);
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isInventoryUsedInRecipes(Long inventoryId) {
        return inventoryRepository.isUsedInRecipes(inventoryId);
    }

    @Override
    public long getRecipeCountByInventory(Long inventoryId) {
        return inventoryRepository.countPublishedRecipesByInventoryId(inventoryId);
    }

    @Override
    public List<InventoryDto> getUnusedInventory() {
        return inventoryRepository.findUnusedInventory().stream()
                .map(inventoryConverter::toDto)
                .collect(Collectors.toList());
    }
}