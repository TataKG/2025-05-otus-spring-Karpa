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
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.CommentMongo;

@Configuration
@RequiredArgsConstructor
public class CommentMigrationConfig {

    private final MongoTemplate mongoTemplate;
    private final JobRepository jobRepository;
    private final EntityManagerFactory entityManagerFactory;
    private final PlatformTransactionManager platformTransactionManager;
    private final IdMappingCache idMappingCache;

    // Reader
    @Bean
    @StepScope
    public JpaCursorItemReader<Comment> commentReader() {
        JpaCursorItemReader<Comment> reader = new JpaCursorItemReader<>();
        reader.setName("commentsCursorReader");
        reader.setEntityManagerFactory(entityManagerFactory);
        reader.setQueryString("""
            select c from Comment c 
            join fetch c.book b 
            join fetch b.author 
            join fetch b.genre
            """);
        return reader;
    }

    // Processor
    @Bean
    @StepScope
    public ItemProcessor<Comment, CommentMongo> commentProcessor() {
        return this::getCommentMongo;
    }

    private CommentMongo getCommentMongo(Comment comment) {
        String mongoId = new ObjectId().toString();

        String bookMongoId = idMappingCache.getBookMongoId(comment.getBook().getId());
        if (bookMongoId == null) {
            throw new IllegalStateException("Book mapping not found for ID: " + comment.getBook().getId());
        }

        return new CommentMongo(mongoId, comment.getText(), bookMongoId);
    }

    // Writer
    @Bean
    @StepScope
    public MongoItemWriter<CommentMongo> commentWriter() {
        MongoItemWriter<CommentMongo> writer = new MongoItemWriter<>();
        writer.setTemplate(mongoTemplate);
        writer.setCollection("comments");
        writer.setMode(MongoItemWriter.Mode.INSERT);
        return writer;
    }

    // Step
    @Bean
    public Step commentMigrationStep(ItemReader<Comment> reader,
                                     ItemProcessor<Comment, CommentMongo> processor,
                                     ItemWriter<CommentMongo> writer) {
        return new StepBuilder("commentMigrationStep", jobRepository)
                .<Comment, CommentMongo>chunk(10, platformTransactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }
}
