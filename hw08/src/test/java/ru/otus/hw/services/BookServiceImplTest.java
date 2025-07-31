package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import ru.otus.hw.converters.AuthorDtoConverter;
import ru.otus.hw.converters.BookDtoConverter;
import ru.otus.hw.converters.GenreDtoConverter;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.repositories.CommentRepository;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Сервис книг")
@DataMongoTest
@Import({BookServiceImpl.class,
        BookDtoConverter.class,
        AuthorDtoConverter.class,
        GenreDtoConverter.class})
class BookServiceImplTest {
    @Autowired
    private BookService bookService;

    @Autowired
    private CommentRepository CommentRepository;

    @Test
    @DisplayName("должен получить корректное число книг в БД")
    void shouldReturnCorrectBookCount() {
        var actualBook = bookService.findAll();
        assertThat(actualBook).hasSize(3);
    }

    @Test
    @DisplayName("должен добавлять новую книгу")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldSaveNewBook() {
        var author = new AuthorDto("1", "Author_1");
        var genre = new GenreDto("2", "Genre_2");
        var expectedBook = new BookDto(null, "BookTitle_10500", author, genre);

        BookDto actualBook = bookService.insert("BookTitle_10500", "1", "2");

        assertThat(actualBook.id())
                .isNotNull()
                .isNotEmpty()
                .isNotBlank();
        assertThat(actualBook)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(expectedBook);
    }

    @Test
    @DisplayName("должен изменять книгу")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldUpdateBook() {
        bookService.findById("1").ifPresent(bookDto -> {
            BookDto beforeUpdate = bookService.update("1", "BookTitle_Updated", "1", "2");
            assertThat(bookDto).isNotEqualTo(beforeUpdate);
        });
        bookService.findById("1").ifPresent(bookDto -> {
            assertThat(bookDto.title()).isEqualTo("BookTitle_Updated");
        });
    }
}