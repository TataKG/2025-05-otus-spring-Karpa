package ru.otus.hw.controllers.pages;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import ru.otus.hw.converters.AuthorDtoConverter;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.models.Author;
import ru.otus.hw.repositories.AuthorRepository;

import java.util.List;

import static org.mockito.BDDMockito.given;

@WebFluxTest(AuthorPagesController.class)
@DisplayName("Тест контроллера страниц авторов")
class AuthorPagesControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private AuthorRepository authorRepository;

    @MockBean
    private AuthorDtoConverter authorDtoConverter;

    private final Author author1 = new Author("68e36f0b10ca0909273327b6", "Лев Толстой");
    private final Author author2 = new Author("68e36f0b10ca0909273327b7", "Фёдор Достоевский");

    private final AuthorDto authorDto1 = new AuthorDto("68e36f0b10ca0909273327b6", "Лев Толстой");
    private final AuthorDto authorDto2 = new AuthorDto("68e36f0b10ca0909273327b7", "Фёдор Достоевский");

    @Test
    @DisplayName("Должен вызывать репозиторий для получения авторов")
    void shouldCallAuthorRepository() {
        // given
        given(authorRepository.findAll()).willReturn(Flux.fromIterable(List.of(author1, author2)));
        given(authorDtoConverter.toDto(author1)).willReturn(authorDto1);
        given(authorDtoConverter.toDto(author2)).willReturn(authorDto2);

        // when & then
        webTestClient.get()
                .uri("/authors")
                .exchange()
                .expectStatus().isOk();
    }
}