package ru.otus.hw.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.otus.hw.models.Genre;

import javax.annotation.Nonnull;
import java.util.List;

public interface GenreRepository extends CrudRepository<Genre, Long> {
    @Override
    @Nonnull
    List<Genre> findAll();
}
