package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Author;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class JdbcAuthorRepository implements AuthorRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcOperations;

    @Override
    public List<Author> findAll() {
        String sql = """
                SELECT id, full_name
                FROM authors
                """;

        return Optional.ofNullable(namedParameterJdbcOperations.query(
                sql, new AuthorRowMapper())).orElse(Collections.emptyList());
    }

    @Override
    public Optional<Author> findById(long id) {
        String sql = """
                SELECT id, full_name
                FROM authors
                WHERE id = :id
                """;
        try {
            Author author = namedParameterJdbcOperations.queryForObject(sql, Map.of("id", id), new AuthorRowMapper());
            return Optional.ofNullable(author);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private static class AuthorRowMapper implements RowMapper<Author> {

        @Override
        public Author mapRow(ResultSet rs, int i) throws SQLException {
            long authorId = rs.getLong("id");
            String fullName = rs.getString("full_name");
            return new Author(authorId, fullName);
        }
    }
}
