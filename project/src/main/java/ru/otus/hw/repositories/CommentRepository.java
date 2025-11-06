package ru.otus.hw.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Comment;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends CrudRepository<Comment, Long> {
    List<Comment> findByRecipeId(Long recipeId);
    List<Comment> findByUserId(Long userId);

    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.recipe.id = :recipeId ORDER BY c.createdAt DESC")
    List<Comment> findByRecipeIdWithUser(@Param("recipeId") Long recipeId);

    @Query("SELECT c FROM Comment c JOIN FETCH c.user JOIN FETCH c.recipe WHERE c.id = :id")
    Optional<Comment> findByIdWithUserAndRecipe(@Param("id") Long id);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.recipe.id = :recipeId")
    int countByRecipeId(@Param("recipeId") Long recipeId);
}
