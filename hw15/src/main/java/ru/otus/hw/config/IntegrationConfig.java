package ru.otus.hw.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.messaging.MessageChannel;
import ru.otus.hw.integration.OrderReleaseStrategy;
import ru.otus.hw.dto.Order;
import ru.otus.hw.services.BakeryService;
import ru.otus.hw.services.FlourMillService;
import ru.otus.hw.services.GrainSupplyService;

@Configuration
@EnableIntegration
public class IntegrationConfig {

    @Value("${app.integration.order-channel-capacity:10}")
    private int orderChannelCapacity;

    @Value("${app.integration.polling-rate:1000}")
    private long pollingRate;

    @Value("${app.integration.max-messages-per-poll:2}")
    private int maxMessagesPerPoll;

    @Value("${app.integration.aggregation-timeout:5000}")
    private long aggregationTimeout;

    @Autowired
    private OrderReleaseStrategy orderReleaseStrategy;

    @Bean
    public MessageChannel orderChannel() {
        return MessageChannels.queue(orderChannelCapacity).getObject();
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

    @Bean
    public MessageChannel grainSupplyChannel() {
        return MessageChannels.direct().getObject();
    }

    @Bean(name = PollerMetadata.DEFAULT_POLLER)
    public PollerMetadata poller() {
        return Pollers.fixedRate(pollingRate)
                .maxMessagesPerPoll(maxMessagesPerPoll)
                .getObject();
    }

    @Bean
    public IntegrationFlow orderProcessingFlow() {
        return IntegrationFlow.from(orderChannel())
                .log("Получен заказ")
                .transform(Order::items)
                .split()
                .log("Заказ разбит на элементы")
                .channel(flourRequestChannel())
                .get();
    }

    @Bean
    public IntegrationFlow flourProductionFlow(FlourMillService flourMillService) {
        return IntegrationFlow.from(flourRequestChannel())
                .handle(flourMillService, "produceFlour")
                .log("Мука произведена")
                .channel(flourDeliveryChannel())
                .get();
    }

    @Bean
    public IntegrationFlow breadProductionFlow(BakeryService bakeryService) {
        return IntegrationFlow.from(flourDeliveryChannel())
                .handle(bakeryService, "produceBread")
                .log("Хлеб произведен")
                .channel(breadProductionChannel())
                .get();
    }

    @Bean
    public IntegrationFlow orderAggregationFlow() {
        return IntegrationFlow.from(breadProductionChannel())
                .aggregate(aggregator -> aggregator
                        .releaseStrategy(orderReleaseStrategy)
                        .groupTimeout(aggregationTimeout)
                        .expireGroupsUponTimeout(true)
                        .sendPartialResultOnExpiry(true)
                )
                .log("Заказ собран")
                .channel(breadDeliveryChannel())
                .get();
    }

    @Bean
    public IntegrationFlow grainProcessingFlow(GrainSupplyService grainSupplyService) {
        return IntegrationFlow.from(grainSupplyChannel())
                .handle(grainSupplyService, "processGrainDelivery")
                .channel(flourDeliveryChannel())
                .get();
    }
}