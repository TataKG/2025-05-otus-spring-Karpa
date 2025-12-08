package ru.otus.hw.converters;

import org.springframework.stereotype.Component;
import ru.otus.hw.dto.OrderItem;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderItemConverter {

    public String formatItems(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return "Нет позиций";
        }

        return items.stream()
                .map(item -> item.productName() + "(" + item.quantity() + ")")
                .collect(Collectors.joining(", "));
    }
}