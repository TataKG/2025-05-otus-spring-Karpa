package ru.otus.hw.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import ru.otus.hw.models.Inventory;

import java.util.List;

public interface InventoryRepository extends CrudRepository<Inventory, Long> {
    boolean existsByName(String name);

    @Query("SELECT i FROM Inventory i ORDER BY i.name ASC")
    List<Inventory> findAllByOrderByNameAsc();

    List<Inventory> findByNameContainingIgnoreCase(String name);

    @Query("SELECT i FROM Inventory i WHERE i.name IN :names")
    List<Inventory> findByNames(@Param("names") List<String> names);

    @Query("SELECT i FROM Inventory i JOIN i.recipes r WHERE r.id = :recipeId")
    List<Inventory> findByRecipeId(@Param("recipeId") Long recipeId);

    @Query("SELECT COUNT(ri) FROM Recipe r JOIN r.inventoryItems ri WHERE ri.id = :inventoryId")
    long countRecipesByInventoryId(@Param("inventoryId") Long inventoryId);

    @Query("SELECT CASE WHEN COUNT(ri) > 0 THEN true ELSE false END " +
            "FROM Recipe r JOIN r.inventoryItems ri WHERE ri.id = :inventoryId")
    boolean isUsedInRecipes(@Param("inventoryId") Long inventoryId);

    @Query("SELECT i FROM Inventory i WHERE i.id NOT IN " +
            "(SELECT DISTINCT ri.id FROM Recipe r JOIN r.inventoryItems ri)")
    List<Inventory> findUnusedInventory();
}