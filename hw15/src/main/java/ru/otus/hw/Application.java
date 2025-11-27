package ru.otus.hw;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.otus.hw.services.FlourMillService;
import ru.otus.hw.services.StoreService;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class Application implements CommandLineRunner {

	private final StoreService storeService;
	private final FlourMillService flourMillService;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Override
	public void run(String... args) {
		log.info("🚀 Запуск системы 'Мукомольный завод - Хлебозавод - Магазин'");

		simulateGrainSupplyIssues();
		storeService.startOrderGeneration();
	}

	private void simulateGrainSupplyIssues() {
		new Thread(() -> {
			try {
				Thread.sleep(15000);
				log.warn("🌾⚡ КРИТИЧЕСКАЯ СИТУАЦИЯ: Зерно закончилось!");
				flourMillService.setHasGrain(false);

				Thread.sleep(10000);
				log.info("🌾✅ СИТУАЦИЯ ВОССТАНОВЛЕНА: Поставка зерна получена!");
				flourMillService.setHasGrain(true);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				log.error("Поток прерван", e);
			}
		}).start();
	}
}