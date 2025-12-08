package ru.otus.hw.services;

import ru.otus.hw.dto.Order;
import ru.otus.hw.dto.Bread;

import java.util.List;

public interface OrderProcessingService {
    List<Bread> processOrder(Order order);

    boolean canProcessOrder(Order order);
}
