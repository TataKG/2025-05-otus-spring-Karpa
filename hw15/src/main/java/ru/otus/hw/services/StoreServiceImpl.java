package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import ru.otus.hw.converters.BreadConverter;
import ru.otus.hw.converters.OrderItemConverter;
import ru.otus.hw.dto.Bread;
import ru.otus.hw.dto.Order;
import ru.otus.hw.dto.OrderItem;
import ru.otus.hw.gateway.StoreGateway;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    private static final String[] BREAD_TYPES = {
            "Белый хлеб", "Ржаной хлеб", "Бородинский", "Батон", "Лаваш",
            "Багет", "Кукурузный", "Цельнозерновой"
    };

    private static final int STORE_COUNT = 8;
    private static final int THREAD_POOL_SIZE = 4;
    private static final int ORDER_GENERATION_DELAY_MS = 8000;
    private static final int SHUTDOWN_TIMEOUT_SECONDS = 60;
    private static final int MAX_ORDER_ITEMS = 3;
    private static final int MIN_ORDER_ITEMS = 1;
    private static final int MAX_QUANTITY_PER_ITEM = 4;
    private static final int MIN_QUANTITY_PER_ITEM = 1;

    private final StoreGateway storeGateway;
    private final OrderItemConverter orderItemConverter;
    private final BreadConverter breadConverter;

    private volatile boolean isRunning = false;
    private ExecutorService executorService;
    private final AtomicInteger orderCounter = new AtomicInteger(1);

    @Override
    public void startOrderGeneration() {
        if (isRunning) {
            log.warn("Генерация заказов уже запущена");
            return;
        }

        isRunning = true;
        executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        log.info("🔄 Запуск генерации заказов...");

        for (int storeNumber = 1; storeNumber <= STORE_COUNT; storeNumber++) {
            scheduleOrderProcessing(storeNumber);

            if (storeNumber < STORE_COUNT) {
                sleepSafely();
            }
        }

        shutdownExecutorAndWait();
        isRunning = false;
        log.info("🛑 Генерация заказов завершена");
    }

    @Override
    public void stopOrderGeneration() {
        isRunning = false;
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    private void scheduleOrderProcessing(int storeNumber) {
        executorService.execute(() -> processOrderForStore(storeNumber));
    }

    private void processOrderForStore(int storeNumber) {
        try {
            String storeName = "Магазин №" + storeNumber;
            Order order = generateOrder(storeName);

            logOrderCreation(order);

            List<Bread> breads = storeGateway.placeOrder(order);
            processOrderResult(order, breads);

        } catch (Exception e) {
            log.error("💥 Критическая ошибка обработки заказа для магазина {}: {}",
                    storeNumber, e.getMessage(), e);
        }
    }

    private void logOrderCreation(Order order) {
        String formattedItems = orderItemConverter.formatItems(order.items());
        log.info("🛒 {} размещает заказ ({} позиций): {} (ID: {})",
                order.storeName(), order.items().size(),
                formattedItems, order.orderId());
    }

    private void processOrderResult(Order order, List<Bread> breads) {
        if (CollectionUtils.isEmpty(breads)) {
            log.error("❌ {} НЕ получил хлеб! Заказ потерян (ID: {})",
                    order.storeName(), order.orderId());
        } else {
            String formattedBreads = breadConverter.formatBreads(breads);
            log.info("✅ 📦 {} УСПЕШНО получает заказ ({} позиций): {} (ID: {})",
                    order.storeName(), breads.size(), formattedBreads, order.orderId());
        }
    }

    private Order generateOrder(String storeName) {
        Random random = new Random();
        List<OrderItem> items = generateOrderItems(random);

        String orderId = String.format("ORDER-%s-%d-%d",
                LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")),
                orderCounter.getAndIncrement(),
                random.nextInt(1000));

        return new Order(
                storeName,
                items,
                orderId,
                LocalDateTime.now()
        );
    }

    private List<OrderItem> generateOrderItems(Random random) {
        int itemCount = random.nextInt(MAX_ORDER_ITEMS - MIN_ORDER_ITEMS + 1) + MIN_ORDER_ITEMS;
        List<OrderItem> items = new ArrayList<>(itemCount);

        for (int i = 0; i < itemCount; i++) {
            items.add(generateOrderItem(random));
        }

        return items;
    }

    private OrderItem generateOrderItem(Random random) {
        String product = BREAD_TYPES[random.nextInt(BREAD_TYPES.length)];
        int quantity = random.nextInt(MAX_QUANTITY_PER_ITEM - MIN_QUANTITY_PER_ITEM + 1)
                + MIN_QUANTITY_PER_ITEM;

        return new OrderItem(product, quantity);
    }

    private void sleepSafely() {
        try {
            Thread.sleep(StoreServiceImpl.ORDER_GENERATION_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Поток был прерван во время ожидания");
        }
    }

    private void shutdownExecutorAndWait() {
        if (executorService == null) {
            return;
        }

        executorService.shutdown();

        try {
            if (!executorService.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                log.warn("ExecutorService не завершил работу в течение {} секунд", SHUTDOWN_TIMEOUT_SECONDS);
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executorService.shutdownNow();
            log.warn("Ожидание завершения потоков было прервано");
        }
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }
}