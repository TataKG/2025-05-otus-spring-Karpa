package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class BakerySystemStarter {

    private final StoreService storeService;
    private final FlourMillService flourMillService;

    public void start() {
        log.info("🚀 Запуск системы 'Мукомольный завод - Хлебозавод - Магазин'");

        startGrainSupplySimulation();

        storeService.startOrderGeneration();
    }

    private void startGrainSupplySimulation() {
        new Thread(() -> {
            try {
                Thread.sleep(15000);
                flourMillService.setHasGrain(false);
                Thread.sleep(10000);
                flourMillService.setHasGrain(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}
