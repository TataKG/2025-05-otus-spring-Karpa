package ru.otus.hw.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Genre;

import javax.annotation.Nonnull;

public interface GenreRepository extends ReactiveMongoRepository<Genre, String> {
    @Override
    @Nonnull
    Flux<Genre> findAll();
}
