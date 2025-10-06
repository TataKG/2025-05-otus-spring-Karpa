package ru.otus.hw.controllers.pages;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.services.AuthorService;

import java.util.List;

import static org.mockito.BDDMockito.given;

@WebFluxTest(AuthorPagesController.class)
@DisplayName("Тест контроллера страниц авторов")
class AuthorPagesControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private AuthorService authorService;

    private final AuthorDto author1 = new AuthorDto("68e36f0b10ca0909273327b6", "Лев Толстой");
    private final AuthorDto author2 = new AuthorDto("68e36f0b10ca0909273327b7", "Фёдор Достоевский");

    @Test
    @DisplayName("Должен вызывать сервис для получения авторов")
    void shouldCallAuthorService() {
        // given
        given(authorService.findAll()).willReturn(Flux.fromIterable(List.of(author1, author2)));

        // when & then
        webTestClient.get()
                .uri("/authors")
                .exchange()
                .expectStatus().isOk();
    }
}