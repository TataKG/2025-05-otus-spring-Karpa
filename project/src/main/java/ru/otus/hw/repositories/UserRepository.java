package ru.otus.hw.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.enabled = true")
    List<User> findAllEnabledUsers();

    @Query("SELECT c FROM Comment c JOIN FETCH c.user u JOIN FETCH c.recipe r WHERE c.id = :id")
    Optional<Comment> findByIdWithUserAndRecipe(@Param("id") Long id);

    @Query("SELECT c FROM Comment c JOIN FETCH c.user u WHERE c.recipe.id = :recipeId ORDER BY c.createdAt DESC")
    List<Comment> findByRecipeIdWithUser(@Param("recipeId") Long recipeId);

    @Query(value = "SELECT COUNT(*) FROM users", nativeQuery = true)
    Long countAllUsers();

}
