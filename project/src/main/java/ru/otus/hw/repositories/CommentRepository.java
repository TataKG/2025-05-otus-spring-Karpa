package ru.otus.hw.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Comment;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends CrudRepository<Comment, Long> {
    @EntityGraph(value = "Comment.withUser", type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT c FROM Comment c WHERE c.recipe.id = :recipeId ORDER BY c.createdAt DESC")
    List<Comment> findByRecipeIdWithUser(@Param("recipeId") Long recipeId);

    @EntityGraph(value = "Comment.withUserAndRecipe", type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT c FROM Comment c WHERE c.id = :id")
    Optional<Comment> findByIdWithUserAndRecipe(@Param("id") Long id);

    List<Comment> findByUserId(Long userId);

    int countByRecipeId(Long recipeId);

    void deleteByRecipeId(Long recipeId);
}
