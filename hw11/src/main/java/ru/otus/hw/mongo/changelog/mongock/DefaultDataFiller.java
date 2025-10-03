package ru.otus.hw.mongo.changelog.mongock;

import com.mongodb.DuplicateKeyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

//@Component
//@Slf4j
//@RequiredArgsConstructor
//public class DefaultDataFiller implements ApplicationRunner {
//
//    private final AuthorRepository authorRepository;
//
//    private final BookRepository bookRepository;
//
//    private final GenreRepository genreRepository;
//
//    private final CommentRepository commentRepository;
//
//    private final Scheduler workerPool;
//
//    @Override
//    public void run(ApplicationArguments args) {
//        log.info("Начало инициализации тестовых данных");
//
//        initializeData()
//            .doOnTerminate(() -> log.info("Инициализация данных завершена"))
//            .subscribe();
//    }
//
////    @Transactional
//    public Mono<Void> initializeData() {
//        return bookRepository.deleteAll()
//                .then(authorRepository.deleteAll())
//                .then(genreRepository.deleteAll())
//                .then(createInitialData());
//    }
//
//    private Mono<Void> createInitialData() {
//        return Mono.zip(
//                createAuthors().collectList(),
//                createGenres().collectList()
//        ).flatMap(tuple -> {
//            List<Author> authors = tuple.getT1();
//            List<Genre> genres = tuple.getT2();
//
//            return createBooks(authors, genres).then();
//        });
//    }
//
//    private Flux<Author> createAuthors() {
//        return Flux.just(
//                new Author("Charlotte Bronte"),
//                new Author("Agatha Christie"),
//                new Author("Charles Dickens")
//        ).flatMap(authorRepository::save);
//    }
//
//    private Flux<Genre> createGenres() {
//        return Flux.just(
//                new Genre("Mystery"),
//                new Genre("Romance"),
//                new Genre("Thriller")
//        ).flatMap(genreRepository::save);
//    }
//
//    private Flux<Book> createBooks(List<Author> authors, List<Genre> genres) {
//        return Flux.just(
//                new Book("Jane Eyre", authors.get(0), genres.get(0)),
//                new Book("Shirley", authors.get(0), genres.get(0)),
//                new Book("Murder on the Orient Express", authors.get(1), genres.get(2)),
//                new Book("Death on the Nile", authors.get(1), genres.get(2)),
//                new Book("Oliver Twist", authors.get(2), genres.get(0)),
//                new Book("David Copperfield", authors.get(2), genres.get(1))
//        ).flatMap(bookRepository::save);
//    }
//
//    private Mono<Void> createCommentsForBooks(List<Book> books) {
//        return Flux.fromIterable(books)
//                .flatMap(this::createCommentsForBook)
//                .then();
//    }
//
//    private Flux<Comment> createCommentsForBook(Book book) {
//        String bookId = book.getId();
//        String bookTitle = book.getTitle();
//
//        return Flux.just(
//                new Comment("Great book! I recommended to read '" + bookTitle + "'", bookId),
//                new Comment("So-so", bookId)
//        ).flatMap(commentRepository::save);
//    }
//}
@Component
@Slf4j
@RequiredArgsConstructor
public class DefaultDataFiller implements ApplicationRunner {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    private final GenreRepository genreRepository;
    private final CommentRepository commentRepository;

    // Если используете @Qualifier
    // @Qualifier("databaseScheduler")
    // private final Scheduler scheduler;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Начало инициализации тестовых данных");

        // Используем boundedElastic для блокирующих операций
        Mono.defer(this::initializeData)
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(v -> log.info("Инициализация данных завершена успешно"))
                .doOnError(error -> log.error("Ошибка при инициализации данных", error))
                .subscribe();
    }

    public Mono<Void> initializeData() {
        return checkIfDataExists()
                .publishOn(Schedulers.parallel()) // Для вычислительных операций
                .flatMap(dataExists -> {
                    if (dataExists) {
                        log.info("Данные уже существуют, пропускаем инициализацию");
                        return Mono.empty();
                    } else {
                        log.info("Начинаем инициализацию тестовых данных");
                        return cleanAndCreateData()
                                .subscribeOn(Schedulers.boundedElastic());
                    }
                });
    }

    private Mono<Boolean> checkIfDataExists() {
        return genreRepository.count()
                .map(count -> count > 0)
                .defaultIfEmpty(false);
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
                createGenresSafely().collectList()
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
        ).flatMap(author ->
                authorRepository.findByFullName(author.getFullName())
                        .switchIfEmpty(authorRepository.save(author))
        );
    }

    private Flux<Genre> createGenresSafely() {
        List<String> genreNames = List.of("Mystery", "Romance", "Thriller");

        return Flux.fromIterable(genreNames)
                .flatMap(genreName ->
                        genreRepository.findByName(genreName)
                                .switchIfEmpty(genreRepository.save(new Genre(genreName)))
                                .onErrorResume(DuplicateKeyException.class, e -> {
                                    log.warn("Жанр '{}' уже существует, пропускаем создание", genreName);
                                    return genreRepository.findByName(genreName);
                                })
                );
    }

    private Flux<Book> createBooks(List<Author> authors, List<Genre> genres) {
        Map<String, Author> authorMap = authors.stream()
                .collect(Collectors.toMap(Author::getFullName, Function.identity()));
        Map<String, Genre> genreMap = genres.stream()
                .collect(Collectors.toMap(Genre::getName, Function.identity()));

        return Flux.just(
                        createBookIfPossible("Jane Eyre", "Charlotte Bronte", "Mystery", authorMap, genreMap),
                        createBookIfPossible("Shirley", "Charlotte Bronte", "Mystery", authorMap, genreMap),
                        createBookIfPossible("Murder on the Orient Express", "Agatha Christie", "Thriller", authorMap, genreMap),
                        createBookIfPossible("Death on the Nile", "Agatha Christie", "Thriller", authorMap, genreMap),
                        createBookIfPossible("Oliver Twist", "Charles Dickens", "Mystery", authorMap, genreMap),
                        createBookIfPossible("David Copperfield", "Charles Dickens", "Romance", authorMap, genreMap)
                ).filter(Objects::nonNull)
                .flatMap(bookRepository::save);
    }

    private Book createBookIfPossible(String title, String authorName, String genreName,
                                      Map<String, Author> authorMap, Map<String, Genre> genreMap) {
        Author author = authorMap.get(authorName);
        Genre genre = genreMap.get(genreName);

        if (author != null && genre != null) {
            return new Book(title, author, genre);
        } else {
            log.warn("Не удалось создать книгу '{}': автор={}, жанр={}",
                    title, author != null ? "найден" : "не найден", genre != null ? "найден" : "не найден");
            return null;
        }
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