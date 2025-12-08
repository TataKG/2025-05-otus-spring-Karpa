package ru.otus.hw.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;

@Configuration
@Slf4j
public class ErrorHandlingConfig {

    @Bean
    public IntegrationFlow errorHandlingFlow() {
        return IntegrationFlow.from("errorChannel")
                .handle(message -> {
                    Exception exception = (Exception) message.getPayload();
                    log.error("💥 Ошибка в интеграционном потоке: {}", exception.getMessage());
                })
                .get();
    }
}
