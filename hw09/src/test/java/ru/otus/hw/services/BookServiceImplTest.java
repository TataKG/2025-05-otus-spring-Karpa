package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.converters.AuthorDtoConverter;
import ru.otus.hw.converters.BookDtoConverter;
import ru.otus.hw.converters.CommentDtoConverter;
import ru.otus.hw.converters.GenreDtoConverter;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.mongo.listeners.BookCascadeDeleteListener;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Import({
        BookServiceImpl.class,
        BookDtoConverter.class,
        AuthorDtoConverter.class,
        GenreDtoConverter.class,
        CommentDtoConverter.class,
        BookCascadeDeleteListener.class
})
class BookServiceImplTest extends BaseMongoTest {


    private final BookService bookService;

    @Autowired
    BookServiceImplTest(BookService bookService) {
        this.bookService = bookService;
    }

    @Test
    @DisplayName("Должен находить книгу по ID с автором и жанром")
    void shouldFindBookByIdWithAuthorAndGenre() {
        // given
        Author author = createAuthor("Test Author");
        Genre genre1 = createGenre("Genre 1");
        Book book = createBook("Test Book", author, genre1);

        // when
        Optional<BookDto> result = bookService.findById(book.getId());

        // then
        assertThat(result).isPresent();
        assertAll(
                () -> assertThat(result.get())
                        .extracting(BookDto::title,
                                dto -> dto.author().fullName(),
                                dto -> dto.genre().name())
                        .containsExactly("Test Book", "Test Author", "Genre 1"));
    }

    @Test
    @DisplayName("Должен возвращать все книги с авторами и жанрами")
    void shouldFindAllBooksWithAuthorsAndGenres() {
        // given
        Author author1 = createAuthor("Author 1");
        Author author2 = createAuthor("Author 2");
        Genre genre = createGenre("Common Genre");

        createBook("Book 1", author1, genre);
        createBook("Book 2", author2, genre);

        // when
        List<BookDto> result = bookService.findAll();

        // then
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result)
                        .extracting(BookDto::title)
                        .containsExactlyInAnyOrder("Book 1", "Book 2"),
                () -> assertThat(result)
                        .extracting(dto -> dto.author().fullName())
                        .containsExactlyInAnyOrder("Author 1", "Author 2"),
                () -> assertThat(result)
                        .extracting(dto -> dto.genre().name())
                        .containsOnly("Common Genre")
        );
    }

    @Test
    @DisplayName("Должен создавать новую книгу с автором и жанром")
    void shouldCreateBookWithAuthorAndGenre() {
        // given
        Author author = createAuthor("New Author");
        Genre genre1 = createGenre("Genre A");

        // when
        BookDto result = bookService.insert(
                "New Book Title",
                author.getId(),
                genre1.getId());

        // then
        Book savedBook = mongoTemplate.findById(result.id(), Book.class);
        assertAll(
                () -> assertThat(result.title()).isEqualTo("New Book Title"),
                () -> assertThat(result.author().id()).isEqualTo(author.getId()),
                () -> assertThat(result.genre().id()).isEqualTo(genre1.getId()),
                () -> assertThat(savedBook).isNotNull(),
                () -> {
                    assert savedBook != null;
                    assertThat(savedBook.getAuthor().getId()).isEqualTo(author.getId());
                },
                () -> {
                    assert savedBook != null;
                    assertThat(savedBook.getGenre().getId()).isEqualTo(genre1.getId());
                }
        );
    }

    @Test
    @DisplayName("Должен обновлять книгу с изменением автора и жанра")
    void shouldUpdateBookWithNewAuthorAndGenre() {
        // given
        Author oldAuthor = createAuthor("Old Author");
        Author newAuthor = createAuthor("New Author");
        Genre oldGenre = createGenre("Old Genre");
        Genre newGenre = createGenre("New Genre");
        Book book = createBook("Old Title", oldAuthor, oldGenre);

        // when
        BookDto result = bookService.update(
                book.getId(),
                "New Title",
                newAuthor.getId(),
                newGenre.getId()
        );

        // then
        Book updatedBook = mongoTemplate.findById(book.getId(), Book.class);
        assertAll(
                () -> assertThat(result.title()).isEqualTo("New Title"),
                () -> assertThat(result.author().id()).isEqualTo(newAuthor.getId()),
                () -> assertThat(result.genre().id()).isEqualTo(newGenre.getId()),
                () -> {
                    assert updatedBook != null;
                    assertThat(updatedBook.getTitle()).isEqualTo("New Title");
                },
                () -> {
                    assert updatedBook != null;
                    assertThat(updatedBook.getAuthor().getId()).isEqualTo(newAuthor.getId());
                },
                () -> {
                    assert updatedBook != null;
                    assertThat(updatedBook.getGenre())
                            .extracting(Genre::getId)
                            .isEqualTo(newGenre.getId());
                }
        );
    }

    @Test
    @DisplayName("Должен удалять книгу и связанные комментарии")
    void shouldDeleteBookWithCascade() {
        // given
        Author author = createAuthor("Test Author");
        Genre genre = createGenre("Test Genre");
        Book book = createBook("To Delete", author, genre);
        Comment comment1 = createComment("Comment 1", book);
        Comment comment2 = createComment("Comment 2", book);

        // when
        bookService.deleteById(book.getId());

        // then
        assertThat(mongoTemplate.findById(book.getId(), Book.class)).isNull();
        assertThat(mongoTemplate.findById(comment1.getId(), Comment.class)).isNull();
        assertThat(mongoTemplate.findById(comment2.getId(), Comment.class)).isNull();

    }

}