package ru.otus.hw.services;

import ru.otus.hw.dto.Bread;
import ru.otus.hw.dto.Flour;

public interface BakeryService {
    Bread produceBread(Flour flour);
}
