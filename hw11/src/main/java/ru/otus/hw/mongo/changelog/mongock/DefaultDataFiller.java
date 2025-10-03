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
import ru.otus.hw.models.Comment;
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
                .doOnSuccess(v -> log.info("Инициализация данных завершена успешно"))
                .doOnError(error -> log.error("Ошибка при инициализации данных", error))
                .subscribe();
    }

    @Transactional
    public Mono<Void> initializeData() {
        return cleanAndCreateData().then();
    }

    private Mono<Void> cleanAndCreateData() {
        return commentRepository.deleteAll()
                .then(bookRepository.deleteAll())
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

            return createBooks(authors, genres)
                    .collectList()
                    .flatMap(this::createCommentsForBooks);
        });
    }

    private Flux<Author> createAuthors() {
        return Flux.just(
                new Author("Charlotte Bronte"),
                new Author("Agatha Christie"),
                new Author("Charles Dickens")
        ).flatMap(authorRepository::save);
    }

    private Flux<Genre> createGenres() {
        return Flux.just(
                new Genre("Mystery"),
                new Genre("Romance"),
                new Genre("Thriller"),
                new Genre("True story")
        ).flatMap(genreRepository::save);
    }

    private Flux<Book> createBooks(List<Author> authors, List<Genre> genres) {
        return Flux.just(
                new Book("Jane Eyre", authors.get(0), genres.get(0)),
                new Book("Shirley", authors.get(0), genres.get(0)),
                new Book("Murder on the Orient Express", authors.get(1), genres.get(2)),
                new Book("Death on the Nile", authors.get(1), genres.get(2)),
                new Book("Oliver Twist", authors.get(2), genres.get(0)),
                new Book("David Copperfield", authors.get(2), genres.get(1))
        ).flatMap(bookRepository::save);
    }

    private Mono<Void> createCommentsForBooks(List<Book> books) {
        return Flux.fromIterable(books)
                .flatMap(this::createCommentsForBook)
                .then();
    }

    private Flux<Comment> createCommentsForBook(Book book) {
        String bookId = book.getId();
        String bookTitle = book.getTitle();

        return Flux.just(
                new Comment("Great book! I recommended to read '" + bookTitle + "'", bookId),
                new Comment("So-so", bookId)
        ).flatMap(commentRepository::save);
    }
}
