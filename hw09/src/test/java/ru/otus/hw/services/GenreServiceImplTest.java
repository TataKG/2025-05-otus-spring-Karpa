package ru.otus.hw.services;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.converters.GenreDtoConverter;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({
        GenreDtoConverter.class,
        GenreServiceImpl.class
})
class GenreServiceImplTest extends BaseMongoTest {

    private final GenreService genreService;

    @Autowired
    GenreServiceImplTest(GenreService genreService) {
        this.genreService = genreService;
    }

    @Test
    @DisplayName("Должен возвращать все жанры")
    void shouldFindAllGenres() {
        // given
        Genre genre1 = createGenre("Fantasy");
        Genre genre2 = createGenre("Science Fiction");

        // when
        List<GenreDto> result = genreService.findAll();

        // then
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result)
                        .extracting(GenreDto::name)
                        .containsExactlyInAnyOrder("Fantasy", "Science Fiction"),
                () -> assertThat(result)
                        .extracting(GenreDto::id)
                        .containsExactlyInAnyOrder(genre1.getId(), genre2.getId())
        );
    }

    @Test
    @DisplayName("Должен возвращать пустой список, когда жанров нет")
    void shouldReturnEmptyListWhenNoGenres() {
        List<GenreDto> result = genreService.findAll();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Должен находить жанр по существующему ID")
    void shouldFindGenreById() {
        // given
        Genre savedGenre = createGenre("Test Genre");

        // when
        Optional<GenreDto> result = genreService.findById(savedGenre.getId());

        // then
        assertTrue(result.isPresent());
        assertAll(
                () -> AssertionsForClassTypes.assertThat(result).isPresent(),
                () -> AssertionsForClassTypes.assertThat(result.get())
                        .extracting(GenreDto::id, GenreDto::name)
                        .containsExactly(savedGenre.getId(), "Test Genre")
        );
    }

    @Test
    @DisplayName("Должен возвращать Optional.empty() для некорректного ID")
    void shouldReturnEmptyForInvalidId() {
        // given
        String invalidId = "invalid_id";

        // when
        Optional<GenreDto> result = genreService.findById(invalidId);

        // then
        AssertionsForClassTypes.assertThat(result).isEmpty();
    }
}