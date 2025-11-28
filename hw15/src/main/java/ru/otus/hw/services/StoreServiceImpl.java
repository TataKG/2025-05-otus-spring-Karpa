package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.otus.hw.dto.Bread;
import ru.otus.hw.dto.Order;
import ru.otus.hw.dto.OrderItem;
import ru.otus.hw.gateway.StoreGateway;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    private static final String[] BREAD_TYPES = {
            "Белый хлеб", "Ржаной хлеб", "Бородинский", "Батон", "Лаваш",
            "Багет", "Кукурузный", "Цельнозерновой"
    };

    private final StoreGateway storeGateway;
    private volatile boolean isRunning = false;

    @Override
    public void startOrderGeneration() {
        if (isRunning) {
            log.warn("Генерация заказов уже запущена");
            return;
        }

        isRunning = true;
        log.info("🔄 Запуск генерации заказов...");

        ForkJoinPool pool = ForkJoinPool.commonPool();

        for (int i = 0; i < 8; i++) {
            int storeNumber = i + 1;
            pool.execute(() -> {
                try {
                    Order order = generateOrder("Магазин №" + storeNumber);
                    log.info("🛒 {} размещает заказ ({} позиций): {} (ID: {})",
                            order.storeName(), order.items().size(),
                            formatOrderItems(order.items()), order.orderId());

                    List<Bread> breads = storeGateway.placeOrder(order);

                    if (breads != null && !breads.isEmpty()) {
                        log.info("✅ 📦 {} УСПЕШНО получает заказ ({} позиций): {} (ID: {})",
                                order.storeName(), breads.size(), formatBreads(breads), order.orderId());
                    } else {
                        log.error("❌ {} НЕ получил хлеб! Заказ потерян (ID: {})",
                                order.storeName(), order.orderId());
                    }

                } catch (Exception e) {
                    log.error("💥 Критическая ошибка обработки заказа для магазина {}: {}",
                            storeNumber, e.getMessage());
                }
            });

            try {
                Thread.sleep(8000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        isRunning = false;
        log.info("🛑 Генерация заказов завершена");
    }

    @Override
    public void stopOrderGeneration() {
        isRunning = false;
    }

    private Order generateOrder(String storeName) {
        Random random = new Random();
        int itemCount = random.nextInt(3) + 1;

        List<OrderItem> items = new ArrayList<>();
        for (int i = 0; i < itemCount; i++) {
            String product = BREAD_TYPES[random.nextInt(BREAD_TYPES.length)];
            int quantity = random.nextInt(4) + 1;
            items.add(new OrderItem(product, quantity));
        }

        return new Order(storeName, items, "ORDER-" + System.currentTimeMillis());
    }

    private String formatOrderItems(List<OrderItem> items) {
        return items.stream()
                .map(item -> item.productName() + "(" + item.quantity() + ")")
                .collect(Collectors.joining(", "));
    }

    private String formatBreads(List<Bread> breads) {
        return breads.stream()
                .map(bread -> bread.type() + "(" + bread.quantity() + ")")
                .collect(Collectors.joining(", "));
    }
}