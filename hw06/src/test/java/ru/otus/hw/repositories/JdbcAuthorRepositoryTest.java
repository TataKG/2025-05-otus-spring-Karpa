package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Author;


import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Репозиторий на основе Jdbc для работы с авторами ")
@JdbcTest
@Import(JdbcAuthorRepository.class)
class JdbcAuthorRepositoryTest {
    private static final int EXPECTED_AUTHOR_COUNT = 3;

    @Autowired
    JdbcAuthorRepository authorRepository;

    @DisplayName("Должен возвращать ожидаемое число авторов")
    @Test
    void shouldReturnExpectedAuthorCount() {
        List<Author> allAuthors = authorRepository.findAll();
        assertEquals(EXPECTED_AUTHOR_COUNT, allAuthors.size());
    }

    @DisplayName("Должен возвращать жанр по его Id")
    @Test
    void shouldReturnAuthorById() {
       var actualAuthor= new Author(2,"Author_2");
       var expectedAuthor = authorRepository.findById(2).orElse(null);
       assertEquals(expectedAuthor, actualAuthor);
    }
}