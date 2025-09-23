package ru.otus.hw.converters;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookFormDto;
import ru.otus.hw.models.Book;

@RequiredArgsConstructor
@Component
public class BookDtoConverter {
    private final AuthorDtoConverter authorDtoConverter;

    private final GenreDtoConverter genreDtoConverter;

    public String bookDtoToString(BookDto book) {
        return "Id: %s, title: %s, author: {%s}, genre: {%s}".formatted(
                book.id(),
                book.title(),
                authorDtoConverter.authorDtoToString(book.author()),
                genreDtoConverter.genreDtoToString(book.genre()));
    }

    public BookDto toDto(Book book) {
        if (book != null) {
            return new BookDto(
                    book.getId(),
                    book.getTitle(),
                    authorDtoConverter.toDto(book.getAuthor()),
                    genreDtoConverter.toDto(book.getGenre())
            );
        }
        return null;
    }

    public BookFormDto bookDtoToBookFormDto(BookDto book) {
        if (book == null) {
            return null;
        }

        return new BookFormDto(
                book.id(),
                book.title(),
                (book.author() != null) ? book.author().id() : null,
                (book.genre() != null) ? book.genre().id() : null
        );
    }
}