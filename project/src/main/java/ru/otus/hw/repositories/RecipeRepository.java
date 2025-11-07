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
    // Методы для "Мои рецепты" - должны загружать комментарии одним запросом
    @Query("SELECT DISTINCT r FROM Recipe r " +
            "LEFT JOIN FETCH r.category " +
            "LEFT JOIN FETCH r.author " +
//            "LEFT JOIN FETCH r.comments " +  // Добавляем FETCH для комментариев
            "WHERE r.author.id = :authorId")
    List<Recipe> findByAuthorIdWithDetails(@Param("authorId") Long authorId);

    @Query("SELECT DISTINCT r FROM Recipe r " +
            "LEFT JOIN FETCH r.category " +
            "LEFT JOIN FETCH r.author " +
            "LEFT JOIN FETCH r.comments " +  // Добавляем FETCH для комментариев
            "WHERE r.author.id = :authorId AND r.published = true")
    List<Recipe> findPublishedByAuthorIdWithDetails(@Param("authorId") Long authorId);


    // Базовые методы с загрузкой только основных связей (без коллекций)
    @Query("SELECT r FROM Recipe r LEFT JOIN FETCH r.category LEFT JOIN FETCH r.author WHERE r.id = :id")
    Optional<Recipe> findByIdWithBasicRelations(@Param("id") Long id);

    // УДАЛЕН проблемный метод с MultipleBagFetchException
    // @Query("SELECT r FROM Recipe r LEFT JOIN FETCH r.category LEFT JOIN FETCH r.author a LEFT JOIN FETCH a.user LEFT JOIN FETCH r.inventoryItems LEFT JOIN FETCH r.comments c LEFT JOIN FETCH c.user WHERE r.id = :id")

    // Методы для получения всех рецептов (только основные связи)
    @Query("SELECT DISTINCT r FROM Recipe r " +
            "LEFT JOIN FETCH r.category " +
            "LEFT JOIN FETCH r.author")
    List<Recipe> findAllWithBasicAssociations();

    @Query("SELECT DISTINCT r FROM Recipe r " +
            "LEFT JOIN FETCH r.category " +
            "LEFT JOIN FETCH r.author " +
            "WHERE r.published = true")
    List<Recipe> findPublishedRecipesWithBasicAssociations();

    @Query("SELECT DISTINCT r FROM Recipe r " +
            "LEFT JOIN FETCH r.category " +
            "LEFT JOIN FETCH r.author " +
            "WHERE r.published = true")
    List<Recipe> findByPublishedTrueWithBasicAssociations();

    // Методы для поиска по автору (только основные связи)
    @Query("SELECT r FROM Recipe r " +
            "LEFT JOIN FETCH r.category " +
            "LEFT JOIN FETCH r.author " +
            "WHERE r.author.id = :authorId")
    List<Recipe> findByAuthorIdWithBasicDetails(@Param("authorId") Long authorId);

    @Query("SELECT r FROM Recipe r " +
            "LEFT JOIN FETCH r.category " +
            "LEFT JOIN FETCH r.author " +
            "WHERE r.author.id = :authorId AND r.published = true")
    List<Recipe> findPublishedByAuthorIdWithBasicDetails(@Param("authorId") Long authorId);

    // Методы для поиска по категории (только основные связи)
    @Query("SELECT r FROM Recipe r " +
            "LEFT JOIN FETCH r.category " +
            "LEFT JOIN FETCH r.author " +
            "WHERE r.category.id = :categoryId AND r.published = true")
    List<Recipe> findPublishedByCategoryIdWithBasicDetails(@Param("categoryId") Long categoryId);

    // Методы поиска по названию (только основные связи)
    @Query("SELECT DISTINCT r FROM Recipe r " +
            "LEFT JOIN FETCH r.category " +
            "LEFT JOIN FETCH r.author " +
            "WHERE LOWER(r.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<Recipe> findByTitleContainingIgnoreCase(@Param("title") String title);

    @Query("SELECT DISTINCT r FROM Recipe r " +
            "LEFT JOIN FETCH r.category " +
            "LEFT JOIN FETCH r.author " +
            "WHERE LOWER(r.title) LIKE LOWER(CONCAT('%', :title, '%')) AND r.published = true")
    List<Recipe> findPublishedByTitleContainingIgnoreCase(@Param("title") String title);

    // Методы поиска по ингредиентам (только основные связи)
    @Query("SELECT DISTINCT r FROM Recipe r " +
            "JOIN r.ingredients i " +
            "LEFT JOIN FETCH r.category " +
            "LEFT JOIN FETCH r.author " +
            "WHERE LOWER(i) LIKE LOWER(CONCAT('%', :ingredient, '%'))")
    List<Recipe> findByIngredientContaining(@Param("ingredient") String ingredient);

    @Query("SELECT DISTINCT r FROM Recipe r " +
            "JOIN r.ingredients i " +
            "LEFT JOIN FETCH r.category " +
            "LEFT JOIN FETCH r.author " +
            "WHERE LOWER(i) LIKE LOWER(CONCAT('%', :ingredient, '%')) AND r.published = true")
    List<Recipe> findPublishedByIngredientContaining(@Param("ingredient") String ingredient);

    // Методы фильтрации (только основные связи)
    @Query("SELECT DISTINCT r FROM Recipe r " +
            "LEFT JOIN FETCH r.category " +
            "LEFT JOIN FETCH r.author " +
            "WHERE (:title IS NULL OR LOWER(r.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
            "AND (:categoryId IS NULL OR r.category.id = :categoryId) " +
            "AND (:authorId IS NULL OR r.author.id = :authorId)")
    List<Recipe> findByFilters(@Param("title") String title,
                               @Param("categoryId") Long categoryId,
                               @Param("authorId") Long authorId);

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

    // Методы для получения популярных и недавних рецептов (только основные связи)
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

    // Методы для подсчета
    long countByPublishedTrue();
    long countByAuthorId(Long authorId);
    long countByAuthorIdAndPublishedTrue(Long authorId);

    // Дополнительные методы для проверки существования
    boolean existsByIdAndAuthorId(Long id, Long authorId);
    boolean existsByIdAndPublishedTrue(Long id);

    // Методы для пагинации (только основные связи)
    @Query("SELECT r FROM Recipe r " +
            "LEFT JOIN FETCH r.category " +
            "LEFT JOIN FETCH r.author " +
            "WHERE r.published = true " +
            "ORDER BY r.createdAt DESC")
    List<Recipe> findPublishedRecipesWithPagination(Pageable pageable);

    @Query("SELECT r FROM Recipe r " +
            "LEFT JOIN FETCH r.category " +
            "LEFT JOIN FETCH r.author " +
            "WHERE r.author.id = :authorId " +
            "ORDER BY r.createdAt DESC")
    List<Recipe> findByAuthorIdWithPagination(@Param("authorId") Long authorId, Pageable pageable);

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.recipe.id = :recipeId")
    void deleteByRecipeId(@Param("recipeId") Long recipeId);

    @Modifying
    @Query("DELETE FROM Recipe r WHERE r.id = :id")
    void deleteById(@Param("id") Long id);

    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM recipe_inventory WHERE recipe_id = :recipeId")
    void deleteInventoryAssociations(@Param("recipeId") Long recipeId);

    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM recipe_ingredients WHERE recipe_id = :recipeId")
    void deleteIngredients(@Param("recipeId") Long recipeId);
}