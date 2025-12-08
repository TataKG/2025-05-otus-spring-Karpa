package ru.otus.hw.services;

import ru.otus.hw.dto.Flour;
import ru.otus.hw.dto.Grain;

public interface GrainSupplyService {
    Flour processGrainDelivery(Grain grain);
}
