package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Repository
@RequiredArgsConstructor
public class JdbcBookRepository implements BookRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcOperations;

    @Override
    public Optional<Book> findById(long id) {
        String sql = """
                SELECT
                  b.id, b.title, b.author_id, b.genre_id, auth.full_name, gen.name
                FROM books b
                INNER JOIN authors auth ON b.author_id = auth.id
                INNER JOIN genres gen ON b.genre_id = gen.id
                WHERE b.id = :id
                """;
        try {
            Book book = namedParameterJdbcOperations.queryForObject(sql, Map.of("id", id), new BookRowMapper());
            return Optional.ofNullable(book);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Book> findAll() {
        String sql = """
                SELECT
                  b.id, b.title, b.author_id, b.genre_id, auth.full_name, gen.name
                FROM books b
                INNER JOIN authors auth ON b.author_id = auth.id
                INNER JOIN genres gen ON b.genre_id = gen.id
                """;

        return Optional.ofNullable(
                        namedParameterJdbcOperations.query(sql, new BookRowMapper()))
                .orElse(Collections.emptyList());
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == 0) {
            return insert(book);
        }
        return update(book);
    }

    @Override
    public void deleteById(long id) {
        String sql = """
                DELETE
                FROM books
                WHERE id = :bookId
                """;
        namedParameterJdbcOperations.update(sql, new MapSqlParameterSource("bookId", id));
    }

    private Book insert(Book book) {
        var keyHolder = new GeneratedKeyHolder();

        var params = new MapSqlParameterSource();

        params.addValue("title", book.getTitle());
        params.addValue("authorId", book.getAuthor().getId());
        params.addValue("genreId", book.getGenre().getId());

        String sql = """
                INSERT INTO books (title, author_id, genre_id)
                VALUES (:title, :authorId, :genreId)
                """;

        namedParameterJdbcOperations.update(sql, params, keyHolder, new String[]{"id"});

        book.setId(keyHolder.getKeyAs(Long.class));
        return book;
    }

    private Book update(Book book) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", book.getId());
        params.addValue("title", book.getTitle());
        params.addValue("authorId", book.getAuthor().getId());
        params.addValue("genreId", book.getGenre().getId());

        String sql = """
                UPDATE books
                SET title = :title,
                author_id = :authorId, genre_id = :genreId
                WHERE id = :id
                """;

        int updatedBooks = namedParameterJdbcOperations.update(sql, params);
        if (updatedBooks == 0) {
            throw new EntityNotFoundException("Unable to update any records");
        }
        return book;
    }

    private static class BookRowMapper implements RowMapper<Book> {

        @Override
        public Book mapRow(ResultSet rs, int rowNum) throws SQLException {
            long id = rs.getLong("id");
            String title = rs.getString("title");
            long authorId = rs.getLong("author_id");
            long genreId = rs.getLong("genre_id");
            String fullName = rs.getString("full_name");
            String name = rs.getString("name");
            return new Book(id, title, new Author(authorId, fullName), new Genre(genreId, name));
        }
    }
}
