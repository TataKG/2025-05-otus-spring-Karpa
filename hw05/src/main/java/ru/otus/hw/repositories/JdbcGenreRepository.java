package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@RequiredArgsConstructor
@Repository
public class JdbcGenreRepository implements GenreRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcOperations;

    @Override
    public List<Genre> findAll() {
        String sql = """
                select id, name
                FROM genres
                """;
        List<Genre> genres = Optional.ofNullable(namedParameterJdbcOperations.query(
                sql, new JdbcGenreRepository.GenreRowMapper())).orElse(Collections.emptyList());
        return genres;
    }

    @Override
    public Optional<Genre> findById(long id) {
        String sql = """
                select id, name
                FROM genres
                WHERE id = :id
                """;
        return Optional.ofNullable(namedParameterJdbcOperations.queryForObject(
                sql, Map.of("id", id), new JdbcGenreRepository.GenreRowMapper()));
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
