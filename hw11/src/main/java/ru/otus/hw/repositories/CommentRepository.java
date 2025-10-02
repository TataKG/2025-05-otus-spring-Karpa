package ru.otus.hw.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.models.Comment;

import javax.annotation.Nonnull;

public interface CommentRepository extends ReactiveMongoRepository<Comment, String> {
    @Nonnull
    Flux<Comment> findByBookId(String id);

    Mono<Void> deleteAllByBookId(String id);
}
