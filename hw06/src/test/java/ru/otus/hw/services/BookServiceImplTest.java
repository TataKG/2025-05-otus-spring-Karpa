package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.converters.AuthorDtoConverter;
import ru.otus.hw.converters.BookDtoConverter;
import ru.otus.hw.converters.CommentDtoConverter;
import ru.otus.hw.converters.GenreDtoConverter;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.repositories.JpaAuthorRepository;
import ru.otus.hw.repositories.JpaBookRepository;
import ru.otus.hw.repositories.JpaCommentRepository;
import ru.otus.hw.repositories.JpaGenreRepository;


import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Интеграционный тест для сервиса книг")
@DataJpaTest
@Import({
        BookServiceImpl.class,
        BookDtoConverter.class,
        AuthorDtoConverter.class,
        GenreDtoConverter.class,
        CommentDtoConverter.class,
        JpaAuthorRepository.class,
        JpaGenreRepository.class,
        JpaBookRepository.class,
        JpaCommentRepository.class,
})
@Transactional(propagation = Propagation.NEVER)
class BookServiceImplTest {

    private final BookService bookService;


    @Autowired
    BookServiceImplTest(BookService bookService) {
        this.bookService = bookService;
    }

    @Test
    @DisplayName("Должен находить книгу по id с полной информацией")
    void findById_shouldFindBookWithFullInfo() {
        Optional<BookDto> result = bookService.findById(1L);

        assertThat(result).isPresent();
        BookDto book = result.get();

        assertThat(book.id()).isPositive();
        assertThat(book.title()).isNotBlank();

        assertThat(book.author()).isNotNull();
        assertThat(book.author().fullName()).isNotBlank();

        assertThat(book.genre()).isNotNull();
        assertThat(book.genre().name()).isNotBlank();
    }

    @Test
    @DisplayName("Должен возвращать пустой Optional при поиске несуществующей книги")
    void findById_shouldReturnEmptyForNonExistingId() {
        Optional<BookDto> result = bookService.findById(999L);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Должен возвращать список всех книг с полной информацией")
    void findAll_shouldReturnAllBooksWithFullInfo() {
        List<BookDto> result = bookService.findAll();

        assertThat(result).isNotEmpty();
        result.forEach(book -> {
            assertThat(book.id()).isPositive();
            assertThat(book.title()).isNotBlank();

            assertThat(book.author()).isNotNull();
            assertThat(book.author().fullName()).isNotBlank();

            assertThat(book.genre()).isNotNull();
            assertThat(book.genre().name()).isNotBlank();
        });
    }

    @Test
    @DisplayName("Должен удалять книгу по id")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void deleteById_shouldDeleteBook() {
        long bookId = 1L;
        assertThat(bookService.findById(bookId)).isPresent();

        bookService.deleteById(bookId);

        assertThat(bookService.findById(bookId)).isEmpty();
    }


    @Test
    @DisplayName("Должен обновлять существующую книгу")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void update_shouldUpdateExistingBook() {
        long bookId = 1L;
        String updatedTitle = "Updated Title";
        long authorId = 1L;
        long genreId = 2;

        BookDto result = bookService.update(bookId, updatedTitle, authorId, genreId);

        assertThat(result.id()).isEqualTo(bookId);
        assertThat(result.title()).isEqualTo(updatedTitle);
        assertThat(result.author().id()).isEqualTo(authorId);
        assertThat(result.genre().id()).isEqualTo(genreId);
    }

    @Test
    @DisplayName("Должен создавать новую книгу")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void insert_shouldCreateNewBook() {
        String title = "New Book Title";
        long authorId = 1L;
        long genreId = 1L;

        BookDto result = bookService.insert(title, authorId, genreId);

        assertThat(result.id()).isPositive();
        assertThat(result.title()).isEqualTo(title);
        assertThat(result.author().id()).isEqualTo(authorId);
        assertThat(result.genre().id()).isEqualTo(genreId);
    }

}