package ru.otus.hw.controllers.pages;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(BookPagesController.class)
@DisplayName("Тест контроллера страниц книг")
class BookPagesControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @DisplayName("Должен возвращать страницу списка книг для корневого пути")
    void shouldReturnBookListPageForRootPath() {
        webTestClient.get()
                .uri("/")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(html -> html.contains("book-list"));
    }

    @Test
    @DisplayName("Должен возвращать страницу списка книг для /books")
    void shouldReturnBookListPageForBooksPath() {
        webTestClient.get()
                .uri("/books")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(html -> html.contains("book-list"));
    }

    @Test
    @DisplayName("Должен возвращать страницу просмотра книги по id")
    void shouldReturnBookViewPage() {
        String bookId = "68e36f0b10ca0909273327b6";

        webTestClient.get()
                .uri("/books/view/{id}", bookId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(html -> html.contains("book-view"));
    }

    @Test
    @DisplayName("Должен возвращать страницу редактирования существующей книги")
    void shouldReturnBookEditPageForExistingBook() {
        String bookId = "68e36f0b10ca0909273327b6";

        webTestClient.get()
                .uri("/books/edit/{id}", bookId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(html -> html.contains("book-edit"));
    }

    @Test
    @DisplayName("Должен возвращать страницу создания новой книги")
    void shouldReturnBookEditPageForNewBook() {
        webTestClient.get()
                .uri("/books/new")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(html -> html.contains("book-edit"));
    }
}


