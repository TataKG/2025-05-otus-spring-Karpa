package ru.otus.hw.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Inventory;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends CrudRepository<Inventory, Long> {

    List<Inventory> findByNameContainingIgnoreCase(String name);

    Optional<Inventory> findByName(String name);

    @Query("SELECT i FROM Inventory i WHERE i.name IN :names")
    List<Inventory> findByNames(@Param("names") List<String> names); // ДОБАВЛЕН

    Page<Inventory> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Inventory> findAll(Pageable pageable);

    @Query("SELECT i FROM Inventory i " +
            "JOIN i.recipes r " +
            "WHERE r.id = :recipeId")
    List<Inventory> findByRecipeId(@Param("recipeId") Long recipeId);

    @Query("SELECT i FROM Inventory i " +
            "LEFT JOIN i.recipes r " +
            "WHERE r IS NULL")
    List<Inventory> findUnusedInventory();

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
            "FROM Recipe r JOIN r.inventoryItems i " +
            "WHERE i.id = :inventoryId")
    boolean isUsedInRecipes(@Param("inventoryId") Long inventoryId);

    @Query("SELECT COUNT(r) FROM Recipe r JOIN r.inventoryItems i " +
            "WHERE i.id = :inventoryId")
    long countPublishedRecipesByInventoryId(@Param("inventoryId") Long inventoryId);
}