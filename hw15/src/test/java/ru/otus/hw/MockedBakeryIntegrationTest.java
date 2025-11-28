package ru.otus.hw;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.otus.hw.config.AppRunner;
import ru.otus.hw.dto.Bread;
import ru.otus.hw.dto.Flour;
import ru.otus.hw.dto.Order;
import ru.otus.hw.dto.OrderItem;
import ru.otus.hw.services.BakeryService;
import ru.otus.hw.services.FlourMillService;
import ru.otus.hw.gateway.StoreGateway;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@SpringIntegrationTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MockedBakeryIntegrationTest {

    @MockBean
    private AppRunner appRunner;

    @MockBean
    private FlourMillService flourMillService;

    @MockBean
    private BakeryService bakeryService;

    @Autowired
    private StoreGateway storeGateway;

    @BeforeEach
    void setUp() {
        when(flourMillService.hasGrain()).thenReturn(true);
        when(flourMillService.produceFlour(any(OrderItem.class)))
                .thenAnswer(invocation -> {
                    OrderItem item = invocation.getArgument(0);
                    if (item.quantity() <= 0) {
                        throw new IllegalArgumentException("Пропущен невалидный заказ: " + item.quantity());
                    }
                    return new Flour("Wheat", item.quantity() * 2, "MOCK-FLOUR");
                });

        when(bakeryService.produceBread(any(Flour.class)))
                .thenAnswer(invocation -> {
                    Flour flour = invocation.getArgument(0);
                    return new Bread("Хлеб из " + flour.type(), flour.weightKg() / 2, "MOCK-BREAD");
                });
    }

    @Test
    void mockedSuccessfulOrder() {
        // Given
        Order order = new Order("Тестовый магазин",
                List.of(
                        new OrderItem("Белый хлеб", 2),
                        new OrderItem("Ржаной хлеб", 1)
                ),
                "MOCK-ORDER-1"
        );

        // When
        List<Bread> result = storeGateway.placeOrder(order);

        // Then
        assertThat(result).isNotNull().hasSize(2);
        verify(flourMillService, times(2)).produceFlour(any());
        verify(bakeryService, times(2)).produceBread(any());
    }

    @Test
    void mockedSingleItemOrder() {
        // Given
        Order order = new Order("Тестовый магазин",
                List.of(new OrderItem("Багет", 1)),
                "MOCK-ORDER-2"
        );

        // When
        List<Bread> result = storeGateway.placeOrder(order);

        // Then
        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.get(0).quantity()).isEqualTo(1);
        verify(flourMillService, times(1)).produceFlour(any());
        verify(bakeryService, times(1)).produceBread(any());
    }
}