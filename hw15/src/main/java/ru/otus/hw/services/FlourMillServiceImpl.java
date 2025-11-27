package ru.otus.hw.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.otus.hw.dto.Flour;
import ru.otus.hw.dto.OrderItem;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class FlourMillServiceImpl implements FlourMillService {
    private final AtomicInteger batchCounter = new AtomicInteger(1);
    private boolean hasGrain = true;

    @Override
    public Flour produceFlour(OrderItem orderItem) {
        if (orderItem.quantity() <= 0) {
            log.warn("⚠️ Пропущен невалидный заказ: {}", orderItem.productName());
            throw new IllegalArgumentException("Пропущен невалидный заказ: " + orderItem.quantity());
        }

        if (!hasGrain) {
            log.warn("❌ Нет зерна для производства муки для заказа: {}", orderItem.productName());
            throw new RuntimeException("Нет зерна для производства муки для заказа: " + orderItem.productName());
        }

        log.info("🏭 Мукомольный завод: производим муку для {}", orderItem.productName());
        delay(1000);

        String batchId = "FLOUR-" + batchCounter.getAndIncrement();
        Flour flour = new Flour("Wheat", orderItem.quantity() * 2, batchId);

        log.info("✅ Мука произведена: {} кг, партия: {}", flour.weightKg(), flour.batchId());
        return flour;
    }

    @Override
    public void setHasGrain(boolean hasGrain) {
        this.hasGrain = hasGrain;
        log.info("🌾 Статус наличия зерна изменен на: {}", hasGrain ? "ДОСТУПНО" : "ОТСУТСТВУЕТ");
    }

    @Override
    public boolean hasGrain() {
        return hasGrain;
    }

    private void delay(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}