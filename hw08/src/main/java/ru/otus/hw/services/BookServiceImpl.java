package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.converters.BookDtoConverter;
import ru.otus.hw.dto.BookDto;
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
        return bookRepository.findById(id).map(bookDtoConverter::toDto);
    }

    @Override
    public List<BookDto> findAll() {
        return bookRepository.findAll().stream()
                .map(bookDtoConverter::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public BookDto insert(String title, String authorId, String genreId) {
        var book = new Book();
        prepareBook(title, authorId, genreId, book);
        return bookDtoConverter.toDto(bookRepository.save(book));
    }

    @Override
    public BookDto update(String id, String title, String authorId, String genreId) {
        Book book = bookRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Book with id %s not found".formatted(id))
        );
        prepareBook(title, authorId, genreId, book);
        return bookDtoConverter.toDto(bookRepository.save(book));
    }

    @Override
    public void deleteById(String id) {
        bookRepository.deleteById(id);
    }

    private void prepareBook(String title, String authorId, String genreId, Book book) {
        book.setTitle(title);
        var author = authorRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("Author with id %s not found".formatted(authorId)));
        var genre = genreRepository.findById(genreId)
                .orElseThrow(() -> new EntityNotFoundException("Genre with id %s not found".formatted(genreId)));
        book.setAuthor(author);
        book.setGenre(genre);
    }

}
