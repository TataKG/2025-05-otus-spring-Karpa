package ru.otus.hw.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Recipe;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeRepository extends CrudRepository<Recipe, Long> {

    List<Recipe> findByTitleContainingIgnoreCase(String title);
    List<Recipe> findByCategoryId(Long categoryId);
    List<Recipe> findByAuthorId(Long authorId);

    // Оптимизированные методы с JOIN FETCH для избежания N+1
    @Query("SELECT r FROM Recipe r " +
            "JOIN FETCH r.category " +
            "JOIN FETCH r.author " +
            "LEFT JOIN FETCH r.comments " +
            "WHERE r.id = :id")
    Optional<Recipe> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT r FROM Recipe r " +
            "JOIN FETCH r.category " +
            "JOIN FETCH r.author " +
            "ORDER BY r.id")
    List<Recipe> findAllWithCategoryAndAuthor();

    @Query("SELECT r FROM Recipe r " +
            "JOIN FETCH r.category " +
            "JOIN FETCH r.author " +
            "LEFT JOIN FETCH r.inventoryItems " +
            "WHERE r.id = :id")
    Optional<Recipe> findByIdWithAllRelations(@Param("id") Long id);

    @Query("SELECT r FROM Recipe r " +
            "JOIN FETCH r.category " +
            "JOIN FETCH r.author " +
            "WHERE r.category.id = :categoryId")
    List<Recipe> findByCategoryIdWithDetails(@Param("categoryId") Long categoryId);

    @Query("SELECT r FROM Recipe r " +
            "JOIN FETCH r.category " +
            "JOIN FETCH r.author " +
            "WHERE r.author.id = :authorId")
    List<Recipe> findByAuthorIdWithDetails(@Param("authorId") Long authorId);

    // Поиск по ингредиентам - ИСПРАВЛЕННЫЙ запрос
    @Query("SELECT DISTINCT r FROM Recipe r " +
            "JOIN FETCH r.category " +
            "JOIN FETCH r.author " +
            "WHERE EXISTS (SELECT 1 FROM r.ingredients i " +
            "WHERE LOWER(i) LIKE LOWER(CONCAT('%', :ingredient, '%')))")
    List<Recipe> findByIngredientContaining(@Param("ingredient") String ingredient);

    // Поиск по нескольким критериям - ИСПРАВЛЕННЫЙ запрос
    @Query("SELECT r FROM Recipe r " +
            "JOIN FETCH r.category " +
            "JOIN FETCH r.author " +
            "WHERE (:title IS NULL OR LOWER(r.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
            "AND (:categoryId IS NULL OR r.category.id = :categoryId) " +
            "AND (:authorId IS NULL OR r.author.id = :authorId)")
    List<Recipe> findByFilters(@Param("title") String title,
                               @Param("categoryId") Long categoryId,
                               @Param("authorId") Long authorId);

    // ДОБАВИМ базовый метод для получения всех рецептов с пагинацией
    @Query("SELECT r FROM Recipe r " +
            "JOIN FETCH r.category " +
            "JOIN FETCH r.author")
    List<Recipe> findAllWithAssociations();
}