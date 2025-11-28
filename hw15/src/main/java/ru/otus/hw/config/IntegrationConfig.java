package ru.otus.hw.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.messaging.MessageChannel;
import ru.otus.hw.dto.Order;
import ru.otus.hw.services.BakeryService;
import ru.otus.hw.services.FlourMillService;
import ru.otus.hw.services.GrainSupplyService;

@Configuration
@EnableIntegration
public class IntegrationConfig {

    @Bean
    public MessageChannel orderChannel() {
        return MessageChannels.queue(10).getObject();
    }

    @Bean
    public MessageChannel flourRequestChannel() {
        return MessageChannels.direct().getObject();
    }

    @Bean
    public MessageChannel flourDeliveryChannel() {
        return MessageChannels.publishSubscribe().getObject();
    }

    @Bean
    public MessageChannel breadProductionChannel() {
        return MessageChannels.direct().getObject();
    }

    @Bean
    public MessageChannel breadDeliveryChannel() {
        return MessageChannels.publishSubscribe().getObject();
    }

    @Bean(name = PollerMetadata.DEFAULT_POLLER)
    public PollerMetadata poller() {
        return Pollers.fixedRate(1000).maxMessagesPerPoll(2).getObject();
    }

    @Bean
    public IntegrationFlow bakeryFlow(FlourMillService flourMillService,
                                      BakeryService bakeryService) {
        return IntegrationFlow.from(orderChannel())
                .log("📨 Получен заказ")
                .transform(Order::items)
                .split()
                .log("🔪 Разбит на элементы")
                .handle(flourMillService, "produceFlour")
                .log("🏭 Мука произведена")
                .channel(flourDeliveryChannel())
                .handle(bakeryService, "produceBread")
                .log("🍞 Хлеб произведен")
                .channel(breadProductionChannel())
                .aggregate(aggregator -> aggregator
                        .releaseStrategy(group -> {
                            Integer sequenceSize = group.getOne().getHeaders().get("sequenceSize", Integer.class);
                            int currentSize = group.size();
                            boolean shouldRelease = sequenceSize != null && currentSize >= sequenceSize;

                            if (shouldRelease) {
                                System.out.println("✅ Агрегация завершена для группы: " +
                                        currentSize + " из " + sequenceSize + " элементов");
                            }

                            return shouldRelease;
                        })
                        .groupTimeout(5000L)
                        .expireGroupsUponTimeout(true)
                        .sendPartialResultOnExpiry(true)
                )
                .log("📦 Заказ собран")
                .channel(breadDeliveryChannel())
                .get();
    }

    @Bean
    public IntegrationFlow grainProcessingFlow(GrainSupplyService grainSupplyService) {
        return IntegrationFlow.from("grainSupplyChannel")
                .handle(grainSupplyService, "processGrainDelivery")
                .channel(flourDeliveryChannel())
                .get();
    }
}