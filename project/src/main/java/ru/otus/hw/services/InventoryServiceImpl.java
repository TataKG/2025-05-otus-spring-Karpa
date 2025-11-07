package ru.otus.hw.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.converters.InventoryConverter;
import ru.otus.hw.dto.InventoryDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Inventory;
import ru.otus.hw.repositories.InventoryRepository;
import ru.otus.hw.util.MessageProvider;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryConverter inventoryConverter;
    private final MessageProvider messageProvider;

    public InventoryServiceImpl(InventoryRepository inventoryRepository,
                                InventoryConverter inventoryConverter,
                                MessageProvider messageProvider) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryConverter = inventoryConverter;
        this.messageProvider = messageProvider;
    }

    @Override
    public InventoryDto createInventory(String name, String description) {
        Inventory inventory = new Inventory(name, description);
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

        // Меняем только описание, название нельзя менять
        inventory.setDescription(description);

        Inventory updatedInventory = inventoryRepository.save(inventory);
        return inventoryConverter.toDto(updatedInventory);
    }

    @Override
    @Transactional
    public void deleteInventory(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("inventory.not_found", id)
                ));

        // Проверяем, используется ли ингредиент в рецептах
        if (inventoryRepository.isUsedInRecipes(id)) {
            throw new IllegalStateException(
                    messageProvider.getMessage("inventory.cannot_delete_used", id)
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
        return inventoryRepository.countPublishedRecipesByInventoryId(inventoryId);
    }

    @Override
    public List<InventoryDto> getUnusedInventory() {
        return inventoryRepository.findUnusedInventory().stream()
                .map(inventoryConverter::toDto)
                .collect(Collectors.toList());
    }

}
