package ru.otus.hw.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import ru.otus.hw.models.Inventory;

import java.util.List;

public interface InventoryRepository extends CrudRepository<Inventory, Long> {
    boolean existsByName(String name);

    @Query("SELECT i FROM Inventory i ORDER BY i.name ASC")
    List<Inventory> findAllByOrderByNameAsc();

    @EntityGraph(value = "Inventory.withRecipes", type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT i FROM Inventory i ORDER BY i.name ASC")
    List<Inventory> findAllWithRecipes();

    List<Inventory> findByNameContainingIgnoreCase(String name);

    @Query("SELECT i FROM Inventory i WHERE i.name IN :names")
    List<Inventory> findByNames(@Param("names") List<String> names);

    // ИСПРАВЛЕНО: правильный JOIN для связи ManyToMany
    @EntityGraph(value = "Inventory.withRecipes", type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT i FROM Inventory i JOIN i.recipes r WHERE r.id = :recipeId")
    List<Inventory> findByRecipeId(@Param("recipeId") Long recipeId);

    // ИСПРАВЛЕНО: правильный подсчет рецептов для инвентаря
    @Query("SELECT COUNT(r) FROM Recipe r JOIN r.inventoryItems i WHERE i.id = :inventoryId")
    long countRecipesByInventoryId(@Param("inventoryId") Long inventoryId);

    // ИСПРАВЛЕНО: правильная проверка использования в рецептах
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
            "FROM Recipe r JOIN r.inventoryItems i WHERE i.id = :inventoryId")
    boolean isUsedInRecipes(@Param("inventoryId") Long inventoryId);

    // ИСПРАВЛЕНО: правильный поиск неиспользуемого инвентаря
    @Query("SELECT i FROM Inventory i WHERE i.id NOT IN " +
            "(SELECT DISTINCT i.id FROM Recipe r JOIN r.inventoryItems i)")
    List<Inventory> findUnusedInventory();

    // ИСПРАВЛЕНО: правильный подсчет рецептов с группировкой
    @Query("SELECT i, COUNT(r) as recipeCount " +
            "FROM Inventory i LEFT JOIN i.recipes r " +
            "GROUP BY i.id, i.name, i.description, i.createdAt " +
            "ORDER BY i.name")
    List<Object[]> findAllWithRecipeCount();

    // ИСПРАВЛЕНО: правильный подсчет опубликованных рецептов
    @Query("SELECT i, COUNT(r) as publishedRecipeCount " +
            "FROM Inventory i LEFT JOIN i.recipes r " +
            "WHERE (r.published = true OR r IS NULL) " +
            "GROUP BY i.id, i.name, i.description, i.createdAt " +
            "ORDER BY i.name")
    List<Object[]> findAllWithPublishedRecipeCount();

    @EntityGraph(value = "Inventory.withRecipes", type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT i FROM Inventory i WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Inventory> findByNameContainingIgnoreCaseWithRecipes(@Param("name") String name);

    // ИСПРАВЛЕНО: используем стандартный метод Spring Data JPA
    // Spring Data JPA автоматически предоставляет findAllById для списка ID
    // List<Inventory> findAllById(Iterable<Long> ids);

    // ДОБАВЛЕНО: метод для проверки существования всех ID
    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.id IN :ids")
    long countByIdIn(@Param("ids") List<Long> ids);

    // ДОБАВЛЕНО: метод для поиска с пагинацией
    @Query("SELECT i FROM Inventory i ORDER BY i.name ASC")
    List<Inventory> findAllWithPagination(Pageable pageable);
}