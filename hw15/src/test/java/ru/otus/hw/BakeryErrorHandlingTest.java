package ru.otus.hw;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.otus.hw.config.AppRunner;
import ru.otus.hw.dto.Bread;
import ru.otus.hw.dto.Order;
import ru.otus.hw.dto.OrderItem;
import ru.otus.hw.gateway.StoreGateway;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@SpringIntegrationTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BakeryErrorHandlingTest {

    @MockBean
    private AppRunner appRunner;

    @Autowired
    private StoreGateway storeGateway;

    @Test
    void orderWithInvalidItemsIsProcessedGracefully() {
        // Given - только валидные элементы
        Order order = new Order("Тестовый магазин",
                List.of(
                        new OrderItem("Белый хлеб", 2),
                        new OrderItem("Ржаной хлеб", 1)
                ),
                "TEST-ORDER-VALID",
                LocalDateTime.now()
        );

        // When
        List<Bread> result = storeGateway.placeOrder(order);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    void minimumValidOrderProcessedSuccessfully() {
        // Given
        Order order = new Order("Тестовый магазин",
                List.of(new OrderItem("Багет", 1)),
                "TEST-ORDER-MIN",
                LocalDateTime.now()
        );

        // When
        List<Bread> result = storeGateway.placeOrder(order);

        // Then
        assertThat(result).isNotNull().hasSize(1);
        Bread bread = result.get(0);
        assertThat(bread.quantity()).isEqualTo(1);
        assertThat(bread.batchId()).isNotBlank();
    }
}
