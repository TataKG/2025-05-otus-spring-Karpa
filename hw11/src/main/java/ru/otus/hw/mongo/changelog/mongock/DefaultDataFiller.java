package ru.otus.hw.mongo.changelog.mongock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class DefaultDataFiller implements ApplicationRunner {

    private final AuthorRepository authorRepository;

    private final BookRepository bookRepository;

    private final GenreRepository genreRepository;

    private final CommentRepository commentRepository;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Начало инициализации тестовых данных");

        initializeData()
                .doOnTerminate(() -> log.info("Инициализация данных завершена"))
                .subscribe();
    }

    @Transactional
    public Mono<Void> initializeData() {
        return bookRepository.deleteAll()
                .then(authorRepository.deleteAll())
                .then(genreRepository.deleteAll())
                .then(createInitialData());
    }

    private Mono<Void> createInitialData() {
        return Mono.zip(
                createAuthors().collectList(),
                createGenres().collectList()
        ).flatMap(tuple -> {
            List<Author> authors = tuple.getT1();
            List<Genre> genres = tuple.getT2();

            return createBooks(authors, genres).then();
        });
    }

    private Flux<Author> createAuthors() {
        return Flux.just(
                new Author("Лев Толстой"),
                new Author("Федор Достоевский"),
                new Author("Антон Чехов")
        ).flatMap(authorRepository::save);
    }

    private Flux<Genre> createGenres() {
        return Flux.just(
                new Genre("Роман"),
                new Genre("Классика"),
                new Genre("Драма")
        ).flatMap(genreRepository::save);
    }

    private Flux<Book> createBooks(List<Author> authors, List<Genre> genres) {
        return Flux.just(
                new Book("Война и мир", authors.get(0), genres.get(0)),
                new Book("Преступление и наказание", authors.get(1), genres.get(1)),
                new Book("Вишневый сад", authors.get(2), genres.get(2))
        ).flatMap(bookRepository::save);
    }

}
