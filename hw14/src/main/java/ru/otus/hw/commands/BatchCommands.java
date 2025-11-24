package ru.otus.hw.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ru.otus.hw.config.IdMappingCache;

@ShellComponent
@RequiredArgsConstructor
public class BatchCommands {
    private final JobLauncher jobLauncher;
    private final Job migrationJob;
    private final MongoTemplate mongoTemplate;
    private final IdMappingCache idMappingCache;

    @ShellMethod(value = "Запуск миграции данных из H2 в MongoDB", key = "migrate")
    public String migrate() throws Exception {
        JobParameters parameters = new JobParametersBuilder()
                .addLong("startAt", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncher.run(migrationJob, parameters);
        return "Миграция запущена. ID: " + execution.getId() +
                ", Status: " + execution.getStatus();
    }

    @ShellMethod(value = "Перезапуск миграции", key = "restart-migration")
    public String restartMigration() throws Exception {
        JobParameters parameters = new JobParametersBuilder()
                .addLong("restartAt", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncher.run(migrationJob, parameters);
        return "Перезапуск миграции. ID: " + execution.getId() +
                ", Status: " + execution.getStatus();
    }

    @ShellMethod(value = "Статистика мигрированных данных", key = "migration-stats")
    public String migrationStats() {
        long authorsCount = mongoTemplate.getCollection("authors").countDocuments();
        long genresCount = mongoTemplate.getCollection("genres").countDocuments();
        long booksCount = mongoTemplate.getCollection("books").countDocuments();
        long commentsCount = mongoTemplate.getCollection("comments").countDocuments();

        return String.format("""
                Статистика миграции:
                Авторы: %d
                Жанры: %d
                Книги: %d
                Комментарии: %d
                """, authorsCount, genresCount, booksCount, commentsCount);
    }

    @ShellMethod(value = "Очистка данных в MongoDB", key = "clean-mongo")
    public String cleanMongo() {
        mongoTemplate.dropCollection("authors");
        mongoTemplate.dropCollection("genres");
        mongoTemplate.dropCollection("books");
        mongoTemplate.dropCollection("comments");

        idMappingCache.clear();

        return "Данные в MongoDB очищены, кэш маппингов сброшен";
    }
}
