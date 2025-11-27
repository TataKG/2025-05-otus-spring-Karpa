package ru.otus.hw.services;

import ru.otus.hw.dto.Flour;
import ru.otus.hw.dto.OrderItem;

public interface FlourMillService {
    Flour produceFlour(OrderItem orderItem);

    void setHasGrain(boolean hasGrain);

    boolean hasGrain();
}