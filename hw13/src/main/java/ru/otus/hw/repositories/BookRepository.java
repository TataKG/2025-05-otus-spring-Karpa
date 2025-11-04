package ru.otus.hw.repositories;

import jakarta.annotation.Nonnull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import ru.otus.hw.models.Book;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends CrudRepository<Book, Long> {

    @EntityGraph(attributePaths = {"author", "genres"})
    Optional<Book> findById(long id);

    @Nonnull
    @EntityGraph(attributePaths = {"author"})
    @Override
    List<Book> findAll();

    @Query("select b from Book b where b.id = :id")
    Optional<Book> findByIdSmall(@Param("id") long id);
}
