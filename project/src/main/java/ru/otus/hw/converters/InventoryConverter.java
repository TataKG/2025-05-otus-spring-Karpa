package ru.otus.hw.converters;

import org.springframework.stereotype.Component;
import ru.otus.hw.dto.InventoryDto;
import ru.otus.hw.models.Inventory;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class InventoryConverter {

    public InventoryDto toDto(Inventory inventory) {
        if (inventory == null) return null;

        return new InventoryDto(
                inventory.getId(),
                inventory.getName(),
                inventory.getDescription()
        );
    }

    public List<InventoryDto> toDtoList(List<Inventory> inventoryItems) {
        return inventoryItems.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
