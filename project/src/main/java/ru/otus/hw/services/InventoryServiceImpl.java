package ru.otus.hw.services;

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
        System.out.println("Creating inventory with name: '" + name + "', description: '" + description + "'");

        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Название инвентаря не может быть пустым");
        }

        String trimmedName = name.trim();

        // Проверяем существование инвентаря
        Optional<Inventory> existingInventory = inventoryRepository.findByName(trimmedName);
        System.out.println("Inventory exists check for '" + trimmedName + "': " + existingInventory.isPresent());

        if (existingInventory.isPresent()) {
            throw new EntityAlreadyExistsException("Инвентарь с названием '" + trimmedName + "' уже существует");
        }

        Inventory inventory = new Inventory(trimmedName, description != null ? description.trim() : null);
        Inventory savedInventory = inventoryRepository.save(inventory);
        System.out.println("Inventory saved with ID: " + savedInventory.getId());

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
    public boolean deleteInventory(Long id) {
        try {
            // Временно: простое удаление для тестирования
            System.out.println("🔍 Attempting to delete inventory ID: " + id);

            // Проверяем использование через репозиторий
            boolean isUsed = isInventoryUsedInRecipes(id);
            long recipeCount = getRecipeCountByInventory(id);

            System.out.println("📊 Inventory " + id + " usage - isUsed: " + isUsed + ", recipeCount: " + recipeCount);

            if (isUsed) {
                System.out.println("❌ Cannot delete inventory " + id + " - it's used in recipes");
                return false;
            }

            inventoryRepository.deleteById(id);
            System.out.println("✅ Successfully deleted inventory " + id);
            return true;

        } catch (Exception e) {
            System.out.println("❌ Error deleting inventory " + id + ": " + e.getMessage());
            e.printStackTrace();
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
