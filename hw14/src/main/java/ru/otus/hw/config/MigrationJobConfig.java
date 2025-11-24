package ru.otus.hw.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MigrationJobConfig {

    private final JobRepository jobRepository;
    private final Step authorMigrationStep;
    private final Step genreMigrationStep;
    private final Step bookMigrationStep;
    private final Step commentMigrationStep;

    @Bean
    public Job migrationJob() {
        return new JobBuilder("migrationJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(authorMigrationStep)
                .next(genreMigrationStep)
                .next(bookMigrationStep)
                .next(commentMigrationStep)
                .build();
    }
}
