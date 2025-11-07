package ru.otus.hw.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Category;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends CrudRepository<Category, Long> {

    // Базовые методы поиска
    Optional<Category> findByName(String name);
    Optional<Category> findByNameIgnoreCase(String name);
    List<Category> findByNameContainingIgnoreCase(String name);
    boolean existsByName(String name);

    // Методы с пагинацией
    Page<Category> findAll(Pageable pageable);
    Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Методы для загрузки связей
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.recipes WHERE c.id = :id")
    Optional<Category> findByIdWithRecipes(@Param("id") Long id);

    @Query("SELECT DISTINCT c FROM Category c " +
            "LEFT JOIN FETCH c.recipes r " +
            "LEFT JOIN FETCH r.author " +
            "WHERE c.id = :id")
    Optional<Category> findByIdWithRecipesAndAuthors(@Param("id") Long id);

    @Query("SELECT c FROM Category c " +
            "LEFT JOIN FETCH c.recipes")
    List<Category> findAllWithRecipes();

    // Методы для опубликованных рецептов
    @Query("SELECT c FROM Category c " +
            "LEFT JOIN FETCH c.recipes r " +
            "WHERE r.published = true")
    List<Category> findAllWithPublishedRecipes();

    @Query("SELECT c FROM Category c " +
            "JOIN c.recipes r " +
            "WHERE r.published = true " +
            "GROUP BY c.id " +
            "ORDER BY COUNT(r) DESC")
    List<Category> findPopularCategories(Pageable pageable);

    default List<Category> findPopularCategories(int limit) {
        return findPopularCategories(PageRequest.of(0, limit));
    }

    // Методы для подсчета
    @Query("SELECT COUNT(r) FROM Recipe r WHERE r.category.id = :categoryId AND r.published = true")
    long countPublishedRecipesByCategoryId(@Param("categoryId") Long categoryId);

    @Query("SELECT COUNT(r) FROM Recipe r WHERE r.category.id = :categoryId")
    long countAllRecipesByCategoryId(@Param("categoryId") Long categoryId);

    @Query("SELECT c, COUNT(r) as recipeCount FROM Category c " +
            "LEFT JOIN c.recipes r " +
            "WHERE r.published = true OR r IS NULL " +
            "GROUP BY c.id " +
            "ORDER BY recipeCount DESC")
    List<Object[]> findAllWithPublishedRecipeCount();

    // Методы для проверки использования
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
            "FROM Recipe r WHERE r.category.id = :categoryId")
    boolean isUsedInRecipes(@Param("categoryId") Long categoryId);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
            "FROM Recipe r WHERE r.category.id = :categoryId AND r.published = true")
    boolean isUsedInPublishedRecipes(@Param("categoryId") Long categoryId);

    // Методы для поиска по рецептам
    @Query("SELECT c FROM Category c " +
            "JOIN c.recipes r " +
            "WHERE r.author.id = :authorId")
    List<Category> findByAuthorId(@Param("authorId") Long authorId);

    @Query("SELECT c FROM Category c " +
            "JOIN c.recipes r " +
            "WHERE r.author.id = :authorId AND r.published = true")
    List<Category> findByAuthorIdAndPublished(@Param("authorId") Long authorId);

    // Методы сортировки
    List<Category> findAllByOrderByNameAsc();
    List<Category> findAllByOrderByCreatedAtDesc();

    @Query("SELECT c FROM Category c " +
            "ORDER BY c.createdAt DESC")
    List<Category> findRecentCategories(Pageable pageable);

    default List<Category> findRecentCategories(int limit) {
        return findRecentCategories(PageRequest.of(0, limit));
    }

    // Методы для статистики
    @Query("SELECT c.name, COUNT(r) as recipeCount " +
            "FROM Category c " +
            "LEFT JOIN c.recipes r " +
            "WHERE r.published = true " +
            "GROUP BY c.id, c.name " +
            "ORDER BY recipeCount DESC")
    List<Object[]> getCategoryStats();

    // Методы для массовых операций
    @Query("SELECT c FROM Category c WHERE c.name IN :names")
    List<Category> findByNames(@Param("names") List<String> names);

    // Методы для неиспользуемых категорий
    @Query("SELECT c FROM Category c " +
            "LEFT JOIN c.recipes r " +
            "WHERE r IS NULL")
    List<Category> findUnusedCategories();
}