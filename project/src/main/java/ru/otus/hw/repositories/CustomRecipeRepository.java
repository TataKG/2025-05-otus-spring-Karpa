package ru.otus.hw.repositories;

import org.springframework.data.domain.Pageable;
import ru.otus.hw.models.Recipe;

import java.util.List;
import java.util.Optional;

public interface CustomRecipeRepository {
    List<Recipe> findPublishedRecipesWithOptimizedRelations(Pageable pageable);
    Optional<Recipe> findByIdWithOptimizedRelations(Long id);
}
