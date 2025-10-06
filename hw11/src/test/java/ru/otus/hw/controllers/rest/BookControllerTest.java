package ru.otus.hw.controllers.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.converters.BookDtoConverter;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookFormDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.services.BookService;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@WebFluxTest(BookController.class)
@DisplayName("Тест контроллера книг")
class BookControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private BookService bookService;

    @MockBean
    private BookDtoConverter bookConverter;

    private final AuthorDto author1 = new AuthorDto("68e36f0b10ca0909273327b6", "Лев Толстой");
    private final AuthorDto author2 = new AuthorDto("68e36f0b10ca0909273327b7", "Фёдор Достоевский");

    private final GenreDto genre1 = new GenreDto("68e36f0b10ca0909273327c1", "Роман");
    private final GenreDto genre2 = new GenreDto("68e36f0b10ca0909273327c2", "Фантастика");
    private final GenreDto genre3 = new GenreDto("68e36f0b10ca0909273327c3", "Детектив");

    private final BookDto book1 = new BookDto("68e36f0b10ca0909273327b1", "Война и мир", author1, genre1);
    private final BookDto book2 = new BookDto("68e36f0b10ca0909273327b2", "Преступление и наказание", author2, genre1);

    private final BookFormDto bookForm1 = new BookFormDto("68e36f0b10ca0909273327b1", "Война и мир", "68e36f0b10ca0909273327b6", "68e36f0b10ca0909273327c1");
    //private final BookFormDto bookForm2 = new BookFormDto("68e36f0b10ca0909273327b2", "Преступление и наказание", "68e36f0b10ca0909273327b7", "68e36f0b10ca0909273327c2");

    // GET ALL
    @Test
    @DisplayName("Должен возвращать список всех книг")
    void shouldReturnAllBooks() {
        // given
        List<BookDto> books = List.of(book1, book2);
        given(bookService.findAll()).willReturn(Flux.fromIterable(books));

        // when & then
        webTestClient.get()
                .uri("/api/v1/books")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(BookDto.class)
                .hasSize(2)
                .contains(book1, book2);

        verify(bookService, times(1)).findAll();
    }

    @Test
    @DisplayName("Должен возвращать пустой список когда книг нет")
    void shouldReturnEmptyListWhenNoBooks() {
        // given
        given(bookService.findAll()).willReturn(Flux.empty());

        // when & then
        webTestClient.get()
                .uri("/api/v1/books")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(BookDto.class)
                .hasSize(0);

        verify(bookService, times(1)).findAll();
    }

    // GET BY ID
    @Test
    @DisplayName("Должен возвращать книгу по существующему id")
    void shouldReturnBookById() {
        // given
        String bookId = "68e36f0b10ca0909273327b1";
        given(bookService.findById(bookId)).willReturn(Mono.just(book1));
        given(bookConverter.bookDtoToBookFormDto(book1)).willReturn(bookForm1);

        // when & then
        webTestClient.get()
                .uri("/api/v1/books/{id}", bookId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(BookFormDto.class)
                .isEqualTo(bookForm1);

        verify(bookService, times(1)).findById(bookId);
        verify(bookConverter, times(1)).bookDtoToBookFormDto(book1);
    }

    @Test
    @DisplayName("Должен создавать новую книгу")
    void shouldCreateBook() {
        // given
        BookFormDto newBookForm = new BookFormDto(null, "Новая книга", "68e36f0b10ca0909273327b6", "68e36f0b10ca0909273327c1");
        BookDto savedBook = new BookDto("68e36f0b10ca0909273327b3", "Новая книга", author1, genre1);

        given(bookService.insert(newBookForm)).willReturn(Mono.just(savedBook));

        // when & then
        webTestClient.post()
                .uri("/api/v1/books")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(newBookForm))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(BookDto.class)
                .isEqualTo(savedBook);

        verify(bookService, times(1)).insert(newBookForm);
    }

    @Test
    @DisplayName("Должен обновлять существующую книгу")
    void shouldUpdateBook() {
        // given
        String bookId = "68e36f0b10ca0909273327b1";
        BookFormDto updatedBookForm = new BookFormDto(bookId, "Обновленная книга", "68e36f0b10ca0909273327b6", "68e36f0b10ca0909273327c1");
        BookDto updatedBook = new BookDto(bookId, "Обновленная книга", author1, genre1);

        given(bookService.update(updatedBookForm)).willReturn(Mono.just(updatedBook));

        // when & then
        webTestClient.put()
                .uri("/api/v1/books/{id}", bookId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(updatedBookForm))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(BookDto.class)
                .isEqualTo(updatedBook);

        verify(bookService, times(1)).update(updatedBookForm);
    }

    @Test
    @DisplayName("Должен удалять книгу по id")
    void shouldDeleteBook() {
        // given
        String bookId = "68e36f0b10ca0909273327b1";
        given(bookService.deleteById(bookId)).willReturn(Mono.empty());

        // when & then
        webTestClient.delete()
                .uri("/api/v1/books/{id}", bookId)
                .exchange()
                .expectStatus().isNoContent();

        verify(bookService, times(1)).deleteById(bookId);
    }

    @Test
    @DisplayName("Должен успешно обрабатывать удаление несуществующей книги")
    void shouldHandleDeleteOfNonExistentBook() {
        // given
        String nonExistentBookId = "non-existent-id";
        given(bookService.deleteById(nonExistentBookId)).willReturn(Mono.empty());

        // when & then
        webTestClient.delete()
                .uri("/api/v1/books/{id}", nonExistentBookId)
                .exchange()
                .expectStatus().isNoContent();

        verify(bookService, times(1)).deleteById(nonExistentBookId);
    }

    @Test
    @DisplayName("Должен проверять структуру JSON ответа для книги по id")
    void shouldCheckJsonStructureForBookById() {
        // given
        String bookId = "68e36f0b10ca0909273327b1";
        given(bookService.findById(bookId)).willReturn(Mono.just(book1));
        given(bookConverter.bookDtoToBookFormDto(book1)).willReturn(bookForm1);

        // when & then
        webTestClient.get()
                .uri("/api/v1/books/{id}", bookId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(bookForm1.id())
                .jsonPath("$.title").isEqualTo(bookForm1.title())
                .jsonPath("$.authorId").isEqualTo(bookForm1.authorId())
                .jsonPath("$.genreId").isEqualTo(bookForm1.genreId());

        verify(bookService, times(1)).findById(bookId);
        verify(bookConverter, times(1)).bookDtoToBookFormDto(book1);
    }
}