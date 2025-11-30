package ru.otus.hw.health;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MongoHealthIndicator implements HealthIndicator {

    private final MongoTemplate mongoTemplate;

    @Override
    public Health health() {
        try {
            // Выполняем простую команду для проверки соединения
            mongoTemplate.executeCommand("{ ping: 1 }");
            return Health.up()
                    .withDetail("database", "MongoDB")
                    .withDetail("status", "available")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("database", "MongoDB")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
