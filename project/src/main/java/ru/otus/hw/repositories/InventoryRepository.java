package ru.otus.hw.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Inventory;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends CrudRepository<Inventory, Long> {

    // Базовые методы поиска
    List<Inventory> findByNameContainingIgnoreCase(String name);
    Optional<Inventory> findByName(String name);
    List<Inventory> findByNameIn(List<String> names);

    @Query("SELECT i FROM Inventory i WHERE i.name IN :names")
    List<Inventory> findByNames(@Param("names") List<String> names);

    // Методы с пагинацией
    Page<Inventory> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Inventory> findAll(Pageable pageable);

    // Методы для поиска с рецептами
    @Query("SELECT DISTINCT i FROM Inventory i " +
            "LEFT JOIN FETCH i.recipes r " +
            "LEFT JOIN FETCH r.category " +
            "LEFT JOIN FETCH r.author " +
            "WHERE i.id = :id")
    Optional<Inventory> findByIdWithRecipes(@Param("id") Long id);

    @Query("SELECT i FROM Inventory i " +
            "LEFT JOIN FETCH i.recipes " +
            "WHERE i.name LIKE %:name%")
    List<Inventory> findByNameContainingWithRecipes(@Param("name") String name);

    // Методы для популярного инвентаря
    @Query("SELECT i FROM Inventory i " +
            "LEFT JOIN i.recipes r " +
            "WHERE r.published = true " +
            "GROUP BY i.id " +
            "ORDER BY COUNT(r) DESC")
    List<Inventory> findPopularInventory(Pageable pageable);

    default List<Inventory> findPopularInventory(int limit) {
        return findPopularInventory(PageRequest.of(0, limit));
    }

    long countByNameContainingIgnoreCase(String name);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
            "FROM Recipe r JOIN r.inventoryItems i " +
            "WHERE i.id = :inventoryId")
    boolean isUsedInRecipes(@Param("inventoryId") Long inventoryId);

    // ИСПРАВЛЕННЫЙ метод для подсчета рецептов
    @Query("SELECT COUNT(r) FROM Recipe r JOIN r.inventoryItems i " +
            "WHERE i.id = :inventoryId")
    long countPublishedRecipesByInventoryId(@Param("inventoryId") Long inventoryId);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
            "FROM Recipe r JOIN r.inventoryItems i " +
            "WHERE i.id = :inventoryId AND r.published = true")
    boolean isUsedInPublishedRecipes(@Param("inventoryId") Long inventoryId);

    // Методы для поиска по рецептам
    @Query("SELECT i FROM Inventory i " +
            "JOIN i.recipes r " +
            "WHERE r.id = :recipeId")
    List<Inventory> findByRecipeId(@Param("recipeId") Long recipeId);

    @Query("SELECT i FROM Inventory i " +
            "JOIN i.recipes r " +
            "WHERE r.id IN :recipeIds")
    List<Inventory> findByRecipeIds(@Param("recipeIds") List<Long> recipeIds);

    // Методы для неиспользуемого инвентаря
    @Query("SELECT i FROM Inventory i " +
            "LEFT JOIN i.recipes r " +
            "WHERE r IS NULL")
    List<Inventory> findUnusedInventory();

    // Методы сортировки
    List<Inventory> findAllByOrderByNameAsc();
    List<Inventory> findAllByOrderByCreatedAtDesc();

    @Query("SELECT i FROM Inventory i " +
            "ORDER BY i.createdAt DESC")
    List<Inventory> findRecentInventory(Pageable pageable);

    default List<Inventory> findRecentInventory(int limit) {
        return findRecentInventory(PageRequest.of(0, limit));
    }
}
