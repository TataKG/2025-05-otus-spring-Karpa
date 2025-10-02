package ru.otus.hw.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import ru.otus.hw.models.Author;

import javax.annotation.Nonnull;

public interface AuthorRepository extends ReactiveMongoRepository<Author, String> {
    @Override
    @Nonnull
    Flux<Author> findAll();
}