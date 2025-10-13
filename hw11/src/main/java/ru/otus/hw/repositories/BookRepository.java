package ru.otus.hw.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import ru.otus.hw.models.Book;

import javax.annotation.Nonnull;

public interface BookRepository extends ReactiveMongoRepository<Book, String> {
    @Nonnull
    @Override
    Flux<Book> findAll();
}
