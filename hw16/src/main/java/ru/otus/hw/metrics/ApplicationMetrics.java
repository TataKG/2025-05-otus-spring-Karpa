package ru.otus.hw.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static io.micrometer.core.instrument.Gauge.builder;

@Component
@RequiredArgsConstructor
public class ApplicationMetrics {

    private final MeterRegistry meterRegistry;

    public void incrementBookCreated() {
        Counter.builder("app.books.created")
                .description("Total number of books created")
                .register(meterRegistry)
                .increment();
    }

    public void incrementBookUpdated() {
        Counter.builder("app.books.updated")
                .description("Total number of books updated")
                .register(meterRegistry)
                .increment();
    }

    public void incrementBookDeleted() {
        Counter.builder("app.books.deleted")
                .description("Total number of books deleted")
                .register(meterRegistry)
                .increment();
    }

    public void incrementCommentAdded() {
        Counter.builder("app.comments.added")
                .description("Total number of comments added")
                .register(meterRegistry)
                .increment();
    }

    public void recordRequestDuration(String endpoint, long durationMs) {
        Timer.builder("app.http.requests.duration")
                .description("HTTP request duration")
                .tag("endpoint", endpoint)
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }
}
