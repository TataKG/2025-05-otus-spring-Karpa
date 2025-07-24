package ru.otus.hw.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Author;

import java.util.List;
import java.util.Optional;

@Repository
@ConditionalOnProperty(value = "repository.type", havingValue = "jpa", matchIfMissing = true)
public class JpaAuthorRepository implements AuthorRepository {
    @PersistenceContext
    private EntityManager entityManager;

    private final Class<Author> entityClass;

    public JpaAuthorRepository(EntityManager entityManager) {
        entityClass = Author.class;
    }

    @Override
    public List<Author> findAll() {
        return entityManager.createQuery("select a from Author a",
                entityClass).getResultList();
    }

    @Override
    public Optional<Author> findById(long id) {
        return Optional.ofNullable(entityManager.find(entityClass, id));
    }
}
