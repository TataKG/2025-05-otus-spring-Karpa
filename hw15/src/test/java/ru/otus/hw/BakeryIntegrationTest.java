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
class BakeryIntegrationTest {

    @MockBean
    private AppRunner appRunner;

    @Autowired
    private StoreGateway storeGateway;

    private static final String BREAD_TYPE1 = "Белый хлеб";
    private static final String BREAD_TYPE2 = "Ржаной хлеб";

    @Test
    void successfulOrderWithOneItem() {
        // Given
        Order order = new Order("Тестовый магазин",
                List.of(new OrderItem(BREAD_TYPE1, 2)),
                "TEST-ORDER-1",
                LocalDateTime.now()
        );

        // When
        List<Bread> result = storeGateway.placeOrder(order);

        // Then
        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.get(0).type()).contains("Хлеб");
        assertThat(result.get(0).quantity()).isGreaterThan(0);
        assertThat(result.get(0).batchId()).isNotBlank();
    }

    @Test
    void successfulOrderWithMultipleItems() {
        // Given
        Order order = new Order("Тестовый магазин",
                List.of(
                        new OrderItem(BREAD_TYPE1, 1),
                        new OrderItem(BREAD_TYPE2, 1)
                ),
                "TEST-ORDER-2",
                LocalDateTime.now()
        );

        // When
        List<Bread> result = storeGateway.placeOrder(order);

        // Then
        assertThat(result).isNotNull().hasSize(2);

        for (Bread bread : result) {
            assertThat(bread.type()).contains("Хлеб");
            assertThat(bread.quantity()).isGreaterThan(0);
        }
    }
}
