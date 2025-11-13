package ru.otus.hw.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Recipe;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeRepository extends CrudRepository<Recipe, Long> {

    @Query("SELECT DISTINCT r FROM Recipe r " +
            "LEFT JOIN FETCH r.category " +
            "LEFT JOIN FETCH r.author " +
            "WHERE r.author.id = :authorId")
    List<Recipe> findByAuthorIdWithDetails(@Param("authorId") Long authorId);

    @Query("SELECT DISTINCT r FROM Recipe r " +
            "LEFT JOIN FETCH r.category " +
            "LEFT JOIN FETCH r.author " +
            "LEFT JOIN FETCH r.comments " +
            "WHERE r.author.id = :authorId AND r.published = true")
    List<Recipe> findPublishedByAuthorIdWithDetails(@Param("authorId") Long authorId);

    @Query("SELECT r FROM Recipe r LEFT JOIN FETCH r.category LEFT JOIN FETCH r.author WHERE r.id = :id")
    Optional<Recipe> findByIdWithBasicRelations(@Param("id") Long id);

    @Query("SELECT DISTINCT r FROM Recipe r " +
            "LEFT JOIN FETCH r.category " +
            "LEFT JOIN FETCH r.author " +
            "WHERE r.published = true")
    List<Recipe> findPublishedRecipesWithBasicAssociations();

    @Query("SELECT DISTINCT r FROM Recipe r " +
            "LEFT JOIN FETCH r.category " +
            "LEFT JOIN FETCH r.author " +
            "WHERE LOWER(r.title) LIKE LOWER(CONCAT('%', :title, '%')) AND r.published = true")
    List<Recipe> findPublishedByTitleContainingIgnoreCase(@Param("title") String title);

    @Query("SELECT DISTINCT r FROM Recipe r " +
            "JOIN r.ingredients i " +
            "LEFT JOIN FETCH r.category " +
            "LEFT JOIN FETCH r.author " +
            "WHERE LOWER(i) LIKE LOWER(CONCAT('%', :ingredient, '%')) AND r.published = true")
    List<Recipe> findPublishedByIngredientContaining(@Param("ingredient") String ingredient);

    @Query("SELECT DISTINCT r FROM Recipe r " +
            "LEFT JOIN FETCH r.category " +
            "LEFT JOIN FETCH r.author " +
            "WHERE r.published = true " +
            "AND (:title IS NULL OR LOWER(r.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
            "AND (:categoryId IS NULL OR r.category.id = :categoryId) " +
            "AND (:authorId IS NULL OR r.author.id = :authorId)")
    List<Recipe> findPublishedByFilters(@Param("title") String title,
                                        @Param("categoryId") Long categoryId,
                                        @Param("authorId") Long authorId);

    @Query("SELECT r FROM Recipe r " +
            "LEFT JOIN FETCH r.category " +
            "LEFT JOIN FETCH r.author " +
            "WHERE r.published = true " +
            "ORDER BY r.createdAt DESC")
    List<Recipe> findRecentPublishedRecipes(Pageable pageable);

    default List<Recipe> findRecentPublishedRecipes(int limit) {
        return findRecentPublishedRecipes(PageRequest.of(0, limit));
    }

    @Query("SELECT r FROM Recipe r " +
            "LEFT JOIN FETCH r.category " +
            "LEFT JOIN FETCH r.author " +
            "WHERE r.published = true " +
            "ORDER BY SIZE(r.comments) DESC, r.createdAt DESC")
    List<Recipe> findPopularPublishedRecipes(Pageable pageable);

    default List<Recipe> findPopularPublishedRecipes(int limit) {
        return findPopularPublishedRecipes(PageRequest.of(0, limit));
    }

    long countByPublishedTrue();

    long countByAuthorId(Long authorId);

    long countByAuthorIdAndPublishedTrue(Long authorId);

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.recipe.id = :recipeId")
    void deleteByRecipeId(@Param("recipeId") Long recipeId);

    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM recipe_inventory WHERE recipe_id = :recipeId")
    void deleteInventoryAssociations(@Param("recipeId") Long recipeId);

    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM recipe_ingredients WHERE recipe_id = :recipeId")
    void deleteIngredients(@Param("recipeId") Long recipeId);
}