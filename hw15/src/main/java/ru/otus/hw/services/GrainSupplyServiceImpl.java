package ru.otus.hw.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.otus.hw.dto.Flour;
import ru.otus.hw.dto.Grain;

@Slf4j
@Service
public class GrainSupplyServiceImpl implements GrainSupplyService {

    @Override
    public Flour processGrainDelivery(Grain grain) {
        log.info("🌾 Обрабатываем поставку зерна: {} кг {}", grain.weightKg(), grain.type());
        delay();

        int flourWeight = grain.weightKg() * 60 / 100;
        Flour flour = new Flour(grain.type(), flourWeight, "GRAIN-" + System.currentTimeMillis());

        log.info("✅ Зерно переработано в муку: {} кг", flour.weightKg());
        return flour;
    }

    private void delay() {
        try {
            Thread.sleep(800);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
