package ru.otus.hw.dto;

import java.util.List;

public record Order(String storeName, List<OrderItem> items, String orderId) {
    public Order {
        items = List.copyOf(items);
    }
}
