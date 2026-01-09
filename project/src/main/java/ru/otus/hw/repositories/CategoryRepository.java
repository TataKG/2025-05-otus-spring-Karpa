package ru.otus.hw.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import ru.otus.hw.models.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends CrudRepository<Category, Long> {
    Optional<Category> findByName(String name);

    Optional<Category> findByNameIgnoreCase(String name);

    boolean existsByName(String name);

    @EntityGraph(value = "Category.withRecipes", type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT c FROM Category c")
    List<Category> findAllWithRecipes();

    List<Category> findAll();

    @Query("SELECT COUNT(r) FROM Recipe r WHERE r.category.id = :categoryId")
    long countRecipesByCategoryId(@Param("categoryId") Long categoryId);

    @Query("SELECT COUNT(r) FROM Recipe r WHERE r.category.id = :categoryId AND r.published = true")
    long countPublishedRecipesByCategoryId(@Param("categoryId") Long categoryId);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
            "FROM Recipe r WHERE r.category.id = :categoryId")
    boolean isUsedInRecipes(@Param("categoryId") Long categoryId);

    @Query("SELECT c FROM Category c WHERE c.id NOT IN (SELECT DISTINCT r.category.id FROM Recipe r)")
    List<Category> findUnusedCategories();

    @Query("SELECT c, COUNT(r) as recipeCount " +
            "FROM Category c LEFT JOIN c.recipes r " +
            "GROUP BY c " +
            "ORDER BY c.name")
    List<Object[]> findAllWithRecipeCount();

    @Query("SELECT c, COUNT(r) as publishedRecipeCount " +
            "FROM Category c LEFT JOIN c.recipes r " +
            "WHERE r.published = true OR r IS NULL " +
            "GROUP BY c " +
            "ORDER BY c.name")
    List<Object[]> findAllWithPublishedRecipeCount();
}