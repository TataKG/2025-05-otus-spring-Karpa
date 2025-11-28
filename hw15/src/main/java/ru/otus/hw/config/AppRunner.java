package ru.otus.hw.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.otus.hw.services.BakerySystemStarter;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppRunner implements CommandLineRunner {

    private final BakerySystemStarter bakerySystemStarter;

    @Override
    public void run(String... args) throws Exception {
        bakerySystemStarter.start();
    }
}
