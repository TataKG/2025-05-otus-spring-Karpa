package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.converters.BookDtoConverter;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookFormDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {
    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    private final BookDtoConverter bookDtoConverter;

    @Override
    public Optional<BookDto> findById(String id) {
        return bookRepository.findById(id)
                .map(bookDtoConverter::toDto);
    }

    @Override
    public List<BookDto> findAll() {
        return bookRepository.findAll().stream()
                .map(bookDtoConverter::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookDto insert(BookFormDto bookDto) {
        return save(bookDto);
    }

    @Override
    @Transactional
    public BookDto update(BookFormDto bookDto) {
        return save(bookDto);
    }

    @Override
    public void deleteById(String id) {
        bookRepository.deleteById(id);
    }

    private Book prepareBook(BookFormDto bookDto) {
        Book book;
        if (bookDto.id() == null || bookDto.id().isEmpty()) {
            book = new Book();
        } else {
            book = bookRepository.findById(bookDto.id()).orElseThrow(
                    ()
                            -> new EntityNotFoundException(
                            "Book with id %s not found".formatted(bookDto.id()))
            );
        }

        book.setTitle(bookDto.title());

        var author = authorRepository.findById(bookDto.authorId())
                .orElseThrow(()
                        -> new EntityNotFoundException(
                        "Author with id %s not found"
                                .formatted(bookDto.authorId())));
        var genre = genreRepository.findById(bookDto.genreId())
                .orElseThrow(()
                        -> new EntityNotFoundException(
                        "Genre with id %s not found"
                                .formatted(bookDto.genreId())));
        book.setAuthor(author);
        book.setGenre(genre);
        return book;
    }

    private BookDto save(BookFormDto bookFormDto) {
        Book book;
        book = prepareBook(bookFormDto);
        return bookDtoConverter.toDto(bookRepository.save(book));
    }
}
