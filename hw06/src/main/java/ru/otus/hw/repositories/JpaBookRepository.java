package ru.otus.hw.repositories;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Book;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.data.jpa.repository.EntityGraph.EntityGraphType.FETCH;

@Repository
@RequiredArgsConstructor
public class JpaBookRepository implements BookRepository {
    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public Optional<Book> findById(long id) {
        Map<String, Object> hints = Collections.singletonMap(FETCH.getKey(), getEntityGraph());
        return Optional.ofNullable(entityManager.find(Book.class, id, hints));
    }

    @Override
    public List<Book> findAll() {
        return entityManager.createQuery("select b from Book b", Book.class)
                .setHint(FETCH.getKey(), getEntityGraph())
                .getResultList();
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == 0) {
            entityManager.persist(book);
            return book;
        }
        return entityManager.merge(book);
    }

    @Override
    public void deleteById(long id) {
        Optional.ofNullable(entityManager.find(Book.class, id))
                .ifPresent(entityManager::remove);
    }

    private EntityGraph<?> getEntityGraph() {
        return entityManager.getEntityGraph("otus-books-author-graph");
    }
}
