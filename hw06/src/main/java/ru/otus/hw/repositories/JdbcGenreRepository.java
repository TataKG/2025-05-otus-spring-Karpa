package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class JdbcGenreRepository implements GenreRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcOperations;

    @Override
    public List<Genre> findAll() {
        String sql = """
                SELECT
                 id, name
                FROM genres
                """;
        return Optional.ofNullable(namedParameterJdbcOperations.query(
                sql, new GenreRowMapper())).orElse(Collections.emptyList());
    }

    @Override
    public Optional<Genre> findById(long id) {
        String sql = """
                SELECT
                  id, name
                FROM genres
                WHERE id = :id
                """;
        try {
            Genre genre = namedParameterJdbcOperations.queryForObject(
                    sql, Map.of("id", id), new JdbcGenreRepository.GenreRowMapper());
            return Optional.ofNullable(genre);
        } catch (
                EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private static class GenreRowMapper implements RowMapper<Genre> {

        @Override
        public Genre mapRow(ResultSet rs, int i) throws SQLException {
            long genreId = rs.getLong("id");
            String name = rs.getString("name");
            return new Genre(genreId, name);
        }
    }
}
