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
import ru.otus.hw.models.Book;
import ru.otus.hw.models.BookMongo;

@Configuration
@RequiredArgsConstructor
public class BookMigrationConfig {
    private final MongoTemplate mongoTemplate;
    private final JobRepository jobRepository;
    private final EntityManagerFactory entityManagerFactory;
    private final PlatformTransactionManager platformTransactionManager;
    private final IdMappingCache idMappingCache;

    // Reader
    @Bean
    @StepScope
    public JpaCursorItemReader<Book> bookReader() {
        JpaCursorItemReader<Book> reader = new JpaCursorItemReader<>();
        reader.setName("booksCursorReader");
        reader.setEntityManagerFactory(entityManagerFactory);
        reader.setQueryString("""
            select b from Book b 
            join fetch b.author 
            join fetch b.genre
            """);
        return reader;
    }

    // Processor
    @Bean
    @StepScope
    public ItemProcessor<Book, BookMongo> bookProcessor() {
        return this::getBookMongo;
    }

    private BookMongo getBookMongo(Book book) {
        String mongoId = new ObjectId().toString();
        idMappingCache.addBookMapItem(book.getId(), mongoId);

        String authorMongoId = idMappingCache.getAuthorMongoId(book.getAuthor().getId());
        String genreMongoId = idMappingCache.getGenreMongoId(book.getGenre().getId());

        if (authorMongoId == null) {
            throw new IllegalStateException("Author mapping not found for ID: " + book.getAuthor().getId());
        }
        if (genreMongoId == null) {
            throw new IllegalStateException("Genre mapping not found for ID: " + book.getGenre().getId());
        }

        return new BookMongo(mongoId, book.getTitle(), authorMongoId, genreMongoId);
    }

    // Writer
    @Bean
    @StepScope
    public MongoItemWriter<BookMongo> bookWriter() {
        MongoItemWriter<BookMongo> writer = new MongoItemWriter<>();
        writer.setTemplate(mongoTemplate);
        writer.setCollection("books");
        writer.setMode(MongoItemWriter.Mode.INSERT);
        return writer;
    }

    // Step
    @Bean
    public Step bookMigrationStep(ItemReader<Book> reader,
                                  ItemProcessor<Book, BookMongo> processor,
                                  ItemWriter<BookMongo> writer) {
        return new StepBuilder("bookMigrationStep", jobRepository)
                .<Book, BookMongo>chunk(10, platformTransactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }
}
