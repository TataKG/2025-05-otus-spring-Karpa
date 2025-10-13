package ru.otus.hw.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Configuration
public class AppConfig {
    @Bean
    public Scheduler workerPool() {
        return Schedulers.newParallel("worker-thread", 2);
    }
}