package ru.otus.hw.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Recipe;

import java.util.List;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class CustomRecipeRepositoryImpl implements CustomRecipeRepository {

    private final EntityManager entityManager;

    @Override
    public List<Recipe> findPublishedRecipesWithOptimizedRelations(Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Recipe> query = cb.createQuery(Recipe.class);
        Root<Recipe> root = query.from(Recipe.class);

        root.fetch("category", JoinType.LEFT);
        root.fetch("author", JoinType.LEFT);
        root.fetch("author.user", JoinType.LEFT);

        query.where(cb.equal(root.get("published"), true))
                .orderBy(cb.desc(root.get("createdAt")));

        return entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
    }

    @Override
    public Optional<Recipe> findByIdWithOptimizedRelations(Long id) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Recipe> query = cb.createQuery(Recipe.class);
        Root<Recipe> root = query.from(Recipe.class);

        root.fetch("category", JoinType.LEFT);
        root.fetch("author", JoinType.LEFT);
        root.fetch("author.user", JoinType.LEFT);
        root.fetch("inventoryItems", JoinType.LEFT);
        root.fetch("comments", JoinType.LEFT);

        query.where(cb.equal(root.get("id"), id));

        try {
            return Optional.of(entityManager.createQuery(query).getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}
