package ru.otus.hw.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Comment;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaCommentRepository implements CommentRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private final Class<Comment> entityClass;

    public JpaCommentRepository(EntityManager entityManager) {
        entityClass = Comment.class;
    }

    @Override
    public Optional<Comment> findById(long id) {
        return Optional.ofNullable(entityManager.find(entityClass, id));
    }

    @Override
    public List<Comment> findByBookId(long bookId) {
        return entityManager.createQuery("select c from Comment c where c.book.id = :bookId", entityClass)
                .setParameter("bookId", bookId)
                .getResultList();
    }

    @Override
    public Comment save(Comment comment) {
        if (comment.getId() == 0) {
            entityManager.persist(comment);
            return comment;
        }
        return entityManager.merge(comment);
    }

    @Override
    public void deleteById(long id) {
        Optional.ofNullable(entityManager.find(entityClass, id))
                .ifPresent(entityManager::remove);
    }

}
