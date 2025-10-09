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
import ru.otus.hw.converters.AuthorDtoConverter;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.models.Author;
import ru.otus.hw.repositories.AuthorRepository;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@WebFluxTest(AuthorController.class)
@DisplayName("Тест контроллера авторов")
class AuthorControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private AuthorRepository authorRepository;

    @MockBean
    private AuthorDtoConverter authorDtoConverter;

    private final Author author1 = new Author("68e36f0b10ca0909273327b6", "Лев Толстой");
    private final Author author2 = new Author("68e36f0b10ca0909273327b7", "Фёдор Достоевский");
    private final Author author3 = new Author("68e36f0b10ca0909273327b8", "Антон Чехов");

    private final AuthorDto authorDto1 = new AuthorDto("68e36f0b10ca0909273327b6", "Лев Толстой");
    private final AuthorDto authorDto2 = new AuthorDto("68e36f0b10ca0909273327b7", "Фёдор Достоевский");
    private final AuthorDto authorDto3 = new AuthorDto("68e36f0b10ca0909273327b8", "Антон Чехов");

    @Test
    @DisplayName("Должен возвращать список всех авторов")
    void shouldReturnAllAuthors() {
        // given
        List<Author> authors = List.of(author1, author2, author3);
        given(authorRepository.findAll()).willReturn(Flux.fromIterable(authors));
        given(authorDtoConverter.toDto(author1)).willReturn(authorDto1);
        given(authorDtoConverter.toDto(author2)).willReturn(authorDto2);
        given(authorDtoConverter.toDto(author3)).willReturn(authorDto3);

        // when & then
        webTestClient.get()
                .uri("/api/v1/authors")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(AuthorDto.class)
                .hasSize(3)
                .contains(authorDto1, authorDto2, authorDto3);

        verify(authorRepository, times(1)).findAll();
        verify(authorDtoConverter, times(1)).toDto(author1);
        verify(authorDtoConverter, times(1)).toDto(author2);
        verify(authorDtoConverter, times(1)).toDto(author3);
    }

    @Test
    @DisplayName("Должен возвращать пустой список когда авторов нет")
    void shouldReturnEmptyListWhenNoAuthors() {
        // given
        given(authorRepository.findAll()).willReturn(Flux.empty());

        // when & then
        webTestClient.get()
                .uri("/api/v1/authors")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(AuthorDto.class)
                .hasSize(0);

        verify(authorRepository, times(1)).findAll();
        verify(authorDtoConverter, never()).toDto(any());
    }

    @Test
    @DisplayName("Должен возвращать автора по существующему id")
    void shouldReturnAuthorById() {
        // given
        String authorId = "68e36f0b10ca0909273327b6";
        given(authorRepository.findById(authorId)).willReturn(Mono.just(author1));
        given(authorDtoConverter.toDto(author1)).willReturn(authorDto1);

        // when & then
        webTestClient.get()
                .uri("/api/v1/authors/{id}", authorId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(AuthorDto.class)
                .isEqualTo(authorDto1);

        verify(authorRepository, times(1)).findById(authorId);
        verify(authorDtoConverter, times(1)).toDto(author1);
    }

    @Test
    @DisplayName("Должен проверять структуру JSON ответа для списка авторов")
    void shouldCheckJsonStructureForAllAuthors() {
        // given
        List<Author> authors = List.of(author1, author2);
        given(authorRepository.findAll()).willReturn(Flux.fromIterable(authors));
        given(authorDtoConverter.toDto(author1)).willReturn(authorDto1);
        given(authorDtoConverter.toDto(author2)).willReturn(authorDto2);

        // when & then
        webTestClient.get()
                .uri("/api/v1/authors")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(authorDto1.id())
                .jsonPath("$[0].fullName").isEqualTo(authorDto1.fullName())
                .jsonPath("$[1].id").isEqualTo(authorDto2.id())
                .jsonPath("$[1].fullName").isEqualTo(authorDto2.fullName());

        verify(authorRepository, times(1)).findAll();
        verify(authorDtoConverter, times(1)).toDto(author1);
        verify(authorDtoConverter, times(1)).toDto(author2);
    }

    @Test
    @DisplayName("Должен проверять структуру JSON ответа для автора по id")
    void shouldCheckJsonStructureForAuthorById() {
        // given
        String authorId = "68e36f0b10ca0909273327b6";
        given(authorRepository.findById(authorId)).willReturn(Mono.just(author1));
        given(authorDtoConverter.toDto(author1)).willReturn(authorDto1);

        // when & then
        webTestClient.get()
                .uri("/api/v1/authors/{id}", authorId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(authorDto1.id())
                .jsonPath("$.fullName").isEqualTo(authorDto1.fullName());

        verify(authorRepository, times(1)).findById(authorId);
        verify(authorDtoConverter, times(1)).toDto(author1);
    }
}