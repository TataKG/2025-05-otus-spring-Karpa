package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Genre;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Репозиторий на основе Jdbc для работы с жанрами ")
@JdbcTest
@Import(JdbcGenreRepository.class)
class JdbcGenreRepositoryTest {
    private static final int EXPECTED_GENRE_COUNT = 3;

    @Autowired
    JdbcGenreRepository genreRepository;

    @DisplayName("Должен возвращать ожидаемое число жанров")
    @Test
    void shouldReturnExpectedGenreCount() {
        List<Genre> allGenre = genreRepository.findAll();
        assertEquals(EXPECTED_GENRE_COUNT, allGenre.size());
    }

    @DisplayName("Должен возвращать жанр по его Id")
    @Test
    void shouldReturnGenreById() {
        var actualGenre = new Genre(2,"Genre_2");
        var expectedGenre = genreRepository.findById(2).orElse(null);
        assertEquals(expectedGenre, actualGenre);
    }
}