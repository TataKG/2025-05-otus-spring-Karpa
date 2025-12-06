package ru.otus.hw.services;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.otus.hw.repositories.BookRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class BooksMetricsService {

    private final BookRepository bookRepository;
    private final MeterRegistry meterRegistry;

    @EventListener(ApplicationReadyEvent.class)
    public void initMetrics() {
        log.info("Books metrics service initialized");
    }

    @Scheduled(fixedRate = 30000)
    public void updateBooksCount() {
        try {
            long count = bookRepository.count();
            log.debug("Current books count: {}", count);
        } catch (Exception e) {
            log.error("Error getting books count: {}", e.getMessage());
        }
    }
}
