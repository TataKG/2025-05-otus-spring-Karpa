package ru.otus.hw.controllers.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.services.AuthorService;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@WebFluxTest(AuthorController.class)
@DisplayName("Тест контроллера авторов")
class AuthorControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private AuthorService authorService;

    private final AuthorDto author1 = new AuthorDto("68e36f0b10ca0909273327b6", "Лев Толстой");
    private final AuthorDto author2 = new AuthorDto("68e36f0b10ca0909273327b7", "Фёдор Достоевский");
    private final AuthorDto author3 = new AuthorDto("68e36f0b10ca0909273327b8", "Антон Чехов");

    @Test
    @DisplayName("Должен возвращать список всех авторов")
    void shouldReturnAllAuthors() {
        // given
        List<AuthorDto> authors = List.of(author1, author2, author3);
        given(authorService.findAll()).willReturn(Flux.fromIterable(authors));

        // when & then
        webTestClient.get()
                .uri("/api/v1/authors")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(AuthorDto.class)
                .hasSize(3)
                .contains(author1, author2, author3);

        verify(authorService, times(1)).findAll();
    }

    @Test
    @DisplayName("Должен возвращать пустой список когда авторов нет")
    void shouldReturnEmptyListWhenNoAuthors() {
        // given
        given(authorService.findAll()).willReturn(Flux.empty());

        // when & then
        webTestClient.get()
                .uri("/api/v1/authors")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(AuthorDto.class)
                .hasSize(0);

        verify(authorService, times(1)).findAll();
    }

    @Test
    @DisplayName("Должен возвращать автора по существующему id")
    void shouldReturnAuthorById() {
        // given
        String authorId = "68e36f0b10ca0909273327b6";
        given(authorService.findById(authorId)).willReturn(Mono.just(author1));

        // when & then
        webTestClient.get()
                .uri("/api/v1/authors/{id}", authorId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(AuthorDto.class)
                .isEqualTo(author1);

        verify(authorService, times(1)).findById(authorId);
    }

    @Test
    @DisplayName("Должен проверять структуру JSON ответа для списка авторов")
    void shouldCheckJsonStructureForAllAuthors() {
        // given
        List<AuthorDto> authors = List.of(author1, author2);
        given(authorService.findAll()).willReturn(Flux.fromIterable(authors));

        // when & then
        webTestClient.get()
                .uri("/api/v1/authors")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(author1.id())
                .jsonPath("$[0].fullName").isEqualTo(author1.fullName())
                .jsonPath("$[1].id").isEqualTo(author2.id())
                .jsonPath("$[1].fullName").isEqualTo(author2.fullName());

        verify(authorService, times(1)).findAll();
    }

    @Test
    @DisplayName("Должен проверять структуру JSON ответа для автора по id")
    void shouldCheckJsonStructureForAuthorById() {
        // given
        String authorId = "68e36f0b10ca0909273327b6";
        given(authorService.findById(authorId)).willReturn(Mono.just(author1));

        // when & then
        webTestClient.get()
                .uri("/api/v1/authors/{id}", authorId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(author1.id())
                .jsonPath("$.fullName").isEqualTo(author1.fullName());

        verify(authorService, times(1)).findById(authorId);
    }
}