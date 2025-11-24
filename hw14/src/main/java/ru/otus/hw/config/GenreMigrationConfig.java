package ru.otus.hw.config;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.MongoItemWriter;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import ru.otus.hw.models.Genre;
import ru.otus.hw.models.GenreMongo;

@Configuration
@RequiredArgsConstructor
public class GenreMigrationConfig {

    private final MongoTemplate mongoTemplate;
    private final JobRepository jobRepository;
    private final EntityManagerFactory entityManagerFactory;
    private final PlatformTransactionManager platformTransactionManager;
    private final IdMappingCache idMappingCache;

    // Reader
    @Bean
    @StepScope
    public JpaCursorItemReader<Genre> genreReader() {
        JpaCursorItemReader<Genre> reader = new JpaCursorItemReader<>();
        reader.setName("genresCursorReader");
        reader.setEntityManagerFactory(entityManagerFactory);
        reader.setQueryString("select g from Genre g");
        return reader;
    }

    // Processor
    @Bean
    @StepScope
    public ItemProcessor<Genre, GenreMongo> genreProcessor() {
        return this::getGenreMongo;
    }

    private GenreMongo getGenreMongo(Genre genre) {
        String mongoId = new ObjectId().toString();
        idMappingCache.addGenreMapItem(genre.getId(), mongoId);
        return new GenreMongo(mongoId, genre.getName());
    }

    // Writer
    @Bean
    @StepScope
    public MongoItemWriter<GenreMongo> genreWriter() {
        MongoItemWriter<GenreMongo> writer = new MongoItemWriter<>();
        writer.setTemplate(mongoTemplate);
        writer.setCollection("genres");
        writer.setMode(MongoItemWriter.Mode.INSERT);
        return writer;
    }

    // Step
    @Bean
    public Step genreMigrationStep(ItemReader<Genre> reader,
                                   ItemProcessor<Genre, GenreMongo> processor,
                                   ItemWriter<GenreMongo> writer) {
        return new StepBuilder("genreMigrationStep", jobRepository)
                .<Genre, GenreMongo>chunk(10, platformTransactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }
}
