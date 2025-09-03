package ru.otus.hw.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Genre;

import java.util.*;

@Repository
public class JpaGenreRepository implements GenreRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private final Class<Genre> entityClass;

    public JpaGenreRepository(EntityManager entityManager) {
        entityClass = Genre.class;
    }

    @Override
    public List<Genre> findAll() {
        return entityManager.createQuery("select g from Genre g",
                entityClass).getResultList();
    }

    @Override
    public Optional<Genre> findById(long id) {
        return Optional.ofNullable(entityManager.find(entityClass, id));
    }
}
