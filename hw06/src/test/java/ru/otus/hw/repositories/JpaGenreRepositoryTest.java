package ru.otus.hw.repositories;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Genre;

import static org.assertj.core.api.Assertions.assertThat;


@DisplayName("Репозиторий на основе Jpa для работы с жанрами ")
@DataJpaTest
@Import(JpaGenreRepository.class)
class JpaGenreRepositoryTest {

    private static final long EXPECTED_GENRE_ID = 1;

    @Autowired
    JpaGenreRepository repositoryJpa;

    @Autowired
    TestEntityManager em;

    @DisplayName("Должен загружать жанр по id")
    @Test
    void shouldFindExpectedGenreById() {
        //given
        val expectedGenre = em.find(Genre.class, EXPECTED_GENRE_ID);
        em.detach(expectedGenre);
        //when
        val optionalActualGenre = repositoryJpa.findById(EXPECTED_GENRE_ID);
        //then
        assertThat(optionalActualGenre).isPresent().get()
                .usingRecursiveComparison().isEqualTo(expectedGenre);
    }
}