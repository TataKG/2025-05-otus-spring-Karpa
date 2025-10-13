package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.converters.BookDtoConverter;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookFormDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

@Slf4j
@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {
    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    private final BookDtoConverter bookDtoConverter;

    @Override
    public Mono<BookDto> findById(String id) {

        return bookRepository.findById(id).map(bookDtoConverter::toDto);
    }

    @Override
    public Flux<BookDto> findAll() {
        return bookRepository.findAll()
                .map(bookDtoConverter::toDto);
    }

    @Override
    public Mono<BookDto> insert(BookFormDto bookDto) {
        log.info("Inserting book: title={}, authorId={}, genreId={}",
                bookDto.title(), bookDto.authorId(), bookDto.genreId());

        if (bookDto.authorId() == null) {
            return Mono.error(new IllegalArgumentException("Author id must not be null"));
        }
        if (bookDto.genreId() == null) {
            return Mono.error(new IllegalArgumentException("Genre id must not be null"));
        }
        return save(bookDto);
    }

    @Override
    public Mono<BookDto> update(BookFormDto bookDto) {
        return save(bookDto);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return bookRepository.deleteById(id);
    }

    private Mono<BookDto> save(BookFormDto bookFormDto) {
        return prepareBook(bookFormDto)
                .doOnNext(book -> log.info("Saving book: {}", book))
                .flatMap(bookRepository::save)
                .doOnNext(savedBook -> log.info("Saved book with id: {}", savedBook.getId()))
                .map(bookDtoConverter::toDto);
    }

    private Mono<Book> prepareBook(BookFormDto bookDto) {
        return getBook(bookDto)
                .zipWith(getAuthor(bookDto))
                .zipWith(getGenre(bookDto))
                .flatMap(tuple
                        -> assembleBook(tuple.getT1().getT1(), tuple.getT1().getT2(), tuple.getT2(), bookDto));
    }

    private Mono<Book> getBook(BookFormDto bookDto) {
        if (bookDto.id() == null || bookDto.id().isEmpty()) {
            return Mono.just(new Book()); // Создаем новый объект каждый раз
        }
        return bookRepository.findById(bookDto.id())
                .switchIfEmpty(Mono.error(
                        new EntityNotFoundException("Book with id %s not found".formatted(bookDto.id()))
                ));
    }

    private Mono<Author> getAuthor(BookFormDto bookDto) {
        return authorRepository.findById(bookDto.authorId())
                .switchIfEmpty(Mono.error(
                        new EntityNotFoundException("Author with id %s not found".formatted(bookDto.authorId()))
                ));
    }

    private Mono<Genre> getGenre(BookFormDto bookDto) {
        return genreRepository.findById(bookDto.genreId())
                .switchIfEmpty(Mono.error(
                        new EntityNotFoundException("Genre with id %s not found".formatted(bookDto.genreId()))
                ));
    }

    private Mono<Book> assembleBook(Book book, Author author,
                                    Genre genre, BookFormDto bookDto) {
        if (bookDto.id() != null && !bookDto.id().isEmpty()) {
            book.setId(bookDto.id());
        }
        book.setTitle(bookDto.title());
        book.setAuthor(author);
        book.setGenre(genre);
        return Mono.just(book);
    }
}
