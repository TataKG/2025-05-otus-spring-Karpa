package ru.otus.hw.dto;

import java.time.LocalDateTime;
import java.util.List;

public record Order(
        String storeName,
        List<OrderItem> items,
        String orderId,
        LocalDateTime createdAt
) {
    Order create(String storeName, List<OrderItem> items) {
        return new Order(
                storeName,
                items,
                "ORDER-" + System.currentTimeMillis(),
                LocalDateTime.now()
        );
    }
}
