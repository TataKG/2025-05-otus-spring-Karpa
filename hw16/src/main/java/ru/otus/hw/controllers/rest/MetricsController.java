package ru.otus.hw.controllers.rest;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.hw.repositories.BookRepository;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
public class MetricsController {

    private final MeterRegistry meterRegistry;
    private final BookRepository bookRepository;

    @GetMapping("/books")
    public Map<String, Object> getBooksMetrics() {
        log.debug("Getting books metrics");

        Map<String, Object> metrics = new HashMap<>();

        try {
            Counter createdCounter = meterRegistry.find("app.books.created").counter();
            Counter updatedCounter = meterRegistry.find("app.books.updated").counter();
            Counter deletedCounter = meterRegistry.find("app.books.deleted").counter();

            long totalBooks = bookRepository.count();

            metrics.put("books_created", createdCounter != null ? (long) createdCounter.count() : 0);
            metrics.put("books_updated", updatedCounter != null ? (long) updatedCounter.count() : 0);
            metrics.put("books_deleted", deletedCounter != null ? (long) deletedCounter.count() : 0);
            metrics.put("total_books", totalBooks);

            metrics.put("timestamp", System.currentTimeMillis());
            metrics.put("status", "success");

        } catch (Exception e) {
            log.error("Error getting books metrics: {}", e.getMessage());
            metrics.put("status", "error");
            metrics.put("message", e.getMessage());
        }

        return metrics;
    }
}
