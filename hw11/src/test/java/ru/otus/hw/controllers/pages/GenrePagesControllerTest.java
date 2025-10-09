package ru.otus.hw.controllers.pages;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import ru.otus.hw.converters.GenreDtoConverter;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;

import static org.mockito.BDDMockito.given;

@WebFluxTest(GenrePagesController.class)
@DisplayName("Тест контроллера страниц жанров")
class GenrePagesControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private GenreRepository genreRepository;

    @MockBean
    private GenreDtoConverter genreDtoConverter;

    private final Genre genre1 = new Genre("68e36f0b10ca0909273327c1", "Роман");
    private final Genre genre2 = new Genre("68e36f0b10ca0909273327c2", "Фантастика");
    private final Genre genre3 = new Genre("68e36f0b10ca0909273327c3", "Детектив");


    private final GenreDto genreDto1 = new GenreDto("68e36f0b10ca0909273327c1", "Роман");
    private final GenreDto genreDto2 = new GenreDto("68e36f0b10ca0909273327c2", "Фантастика");
    private final GenreDto genreDto3 = new GenreDto("68e36f0b10ca0909273327c3", "Детектив");

    @Test
    @DisplayName("Должен возвращать страницу с таблицей жанров когда жанры есть")
    void shouldReturnPageWithGenresTableWhenGenresExist() {
        // given
        given(genreRepository.findAll()).willReturn(Flux.fromIterable(List.of(genre1, genre2)));
        given(genreDtoConverter.toDto(genre1)).willReturn(genreDto1);
        given(genreDtoConverter.toDto(genre2)).willReturn(genreDto2);


        // when & then
        webTestClient.get()
                .uri("/genres")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(html -> {
                    // Проверяем наличие таблицы жанров
                    assert html.contains("<table");
                    assert html.contains("genres-table");
                    assert html.contains("<thead>");
                    assert html.contains("<th>Name</th>");
                    // Проверяем, что предупреждение о пустом списке скрыто
                    assert html.contains("No genres available");
                });
    }

    @Test
    @DisplayName("Должен содержать JavaScript для загрузки жанров через API")
    void shouldContainJavaScriptForLoadingGenres() {
        // given
        given(genreRepository.findAll()).willReturn(Flux.fromIterable(List.of(genre1, genre2)));
        given(genreDtoConverter.toDto(genre1)).willReturn(genreDto1);
        given(genreDtoConverter.toDto(genre2)).willReturn(genreDto2);

        // when & then
        webTestClient.get()
                .uri("/genres")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(html -> {
                    // Проверяем наличие JavaScript кода
                    assert html.contains("fetch('/api/v1/genres')");
                    assert html.contains("document.addEventListener('DOMContentLoaded'");
                    assert html.contains("genres.forEach");
                    assert html.contains("Error fetching genres");
                });
    }
}