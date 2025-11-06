package ru.otus.hw.repositories;

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
    List<Inventory> findByNames(@Param("names") List<String> names);
}