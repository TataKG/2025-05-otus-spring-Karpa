package ru.otus.hw.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.otus.hw.dto.Bread;
import ru.otus.hw.dto.Flour;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class BakeryServiceImpl implements BakeryService {
    private final AtomicInteger breadBatchCounter = new AtomicInteger(1);

    @Override
    public Bread produceBread(Flour flour) {
        log.info("🍞 Хлебозавод: выпекаем хлеб из муки партии {}", flour.batchId());
        delay();

        String breadBatchId = "BREAD-" + breadBatchCounter.getAndIncrement();
        int breadQuantity = flour.weightKg() / 2;
        Bread bread = new Bread("Хлеб из " + flour.type(), breadQuantity, breadBatchId);

        log.info("✅ Хлеб испечен: {} буханок, партия: {}", bread.quantity(), bread.batchId());
        return bread;
    }

    private void delay() {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}