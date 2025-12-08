package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.otus.hw.dto.Bread;
import ru.otus.hw.dto.Order;
import ru.otus.hw.dto.OrderItem;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderProcessingServiceImpl implements OrderProcessingService {

    private final FlourMillService flourMillService;

    @Override
    public List<Bread> processOrder(Order order) {
        log.info("🔄 Начинаем обработку заказа {} для {}", order.orderId(), order.storeName());
        List<Bread> result = new ArrayList<>();

        for (OrderItem item : order.items()) {
            try {
                if (item.quantity() > 0) {
                    flourMillService.produceFlour(item);
                    Bread bread = new Bread(item.productName(), item.quantity(), "BATCH-" + order.orderId());
                    result.add(bread);
                }
            } catch (Exception e) {
                log.warn("⚠️ Не удалось обработать позицию {}: {}", item.productName(), e.getMessage());
                Bread failedBread = new Bread(item.productName(), 0, "FAILED");
                result.add(failedBread);
            }
        }

        log.info("✅ Заказ {} обработан, произведено {} позиций", order.orderId(), result.size());
        return result;
    }

    @Override
    public boolean canProcessOrder(Order order) {
        return flourMillService.hasGrain() &&
                order.items().stream().anyMatch(item -> item.quantity() > 0);
    }
}
