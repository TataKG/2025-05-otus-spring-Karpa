package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.converters.RecipeConverter;
import ru.otus.hw.dto.*;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.*;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.RecipeRepository;
import ru.otus.hw.util.MessageProvider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class RecipeServiceImpl implements RecipeService {
    private final RecipeRepository recipeRepository;
    private final RecipeConverter recipeConverter;
    private final MessageProvider messageProvider;
    private final CommentRepository commentRepository;
    private final CommentService commentService;
    private final CategoryService categoryService;
    private final AuthorService authorService;
    private final InventoryService inventoryService;

    @Override
    public RecipeWithDetailsDto getRecipeWithDetails(Long id) {
        RecipeDto recipe = getRecipeByIdWithBasicRelations(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("recipe.not_found", id)
                ));

        List<InventoryDto> inventory = inventoryService.getInventoryByRecipeId(id);
        List<CommentDto> comments = commentService.getCommentsByRecipeId(id);
        int commentCount = commentService.getCommentCountForRecipe(id);

        return new RecipeWithDetailsDto(recipe, inventory, comments, commentCount);
    }

    @Override
    public Optional<RecipeDto> getRecipeByIdWithBasicRelations(Long id) {
        return recipeRepository.findByIdWithBasicRelations(id)
                .map(recipeConverter::toDto);
    }

    @Override
    public RecipeDto createRecipe(String title, Long categoryId, Long authorId,
                                  List<String> ingredients, String description, boolean published) {
        return createRecipeWithInventory(title, categoryId, authorId, ingredients, description, null, published);
    }

    @Override
    public RecipeDto createRecipeWithInventory(String title, Long categoryId, Long authorId,
                                               List<String> ingredients, String description,
                                               List<Long> inventoryIds, boolean published) {
        try {
            if (title == null || title.trim().isEmpty()) {
                throw new IllegalArgumentException(messageProvider.getMessage("recipe.title.empty"));
            }
            if (categoryId == null) {
                throw new IllegalArgumentException(messageProvider.getMessage("category.id.null"));
            }
            if (authorId == null) {
                throw new IllegalArgumentException(messageProvider.getMessage("author.id.null"));
            }
            if (ingredients == null || ingredients.isEmpty()) {
                throw new IllegalArgumentException(messageProvider.getMessage("recipe.ingredients.empty"));
            }
            if (description == null || description.trim().isEmpty()) {
                throw new IllegalArgumentException(messageProvider.getMessage("recipe.description.empty"));
            }

            List<Long> processedInventoryIds = (inventoryIds != null) ?
                    inventoryIds.stream()
                            .filter(id -> id != null)
                            .collect(Collectors.toList()) :
                    new ArrayList<>();

            CategoryDto categoryDto = categoryService.getCategoryEntityForInternalUse(categoryId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            messageProvider.getMessage("category.not_found", categoryId)
                    ));

            AuthorDto authorDto = authorService.getAuthorForInternalUse(authorId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            messageProvider.getMessage("author.not_found", authorId)
                    ));

            Category category = createCategoryEntityFromDto(categoryDto);
            Author author = createAuthorEntityFromDto(authorDto);

            Recipe recipe = new Recipe(title, category, author, description);

            if (ingredients != null) {
                List<String> validIngredients = ingredients.stream()
                        .filter(ingredient -> ingredient != null && !ingredient.trim().isEmpty())
                        .collect(Collectors.toList());

                if (validIngredients.isEmpty()) {
                    throw new IllegalArgumentException(messageProvider.getMessage("recipe.ingredients.empty"));
                }

                recipe.getIngredients().addAll(validIngredients);
            }

            recipe.setPublished(published);

            if (processedInventoryIds != null && !processedInventoryIds.isEmpty()) {
                List<InventoryDto> inventoryDtos = inventoryService.getInventoryByIdsForInternalUse(processedInventoryIds);

                for (InventoryDto inventoryDto : inventoryDtos) {
                    if (inventoryDto != null) {
                        Inventory inventory = createInventoryEntityFromDto(inventoryDto);
                        recipe.addInventoryItem(inventory);
                    }
                }
            }

            Recipe savedRecipe = recipeRepository.save(recipe);
            return recipeConverter.toDto(savedRecipe);

        } catch (IllegalArgumentException | EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(messageProvider.getMessage("recipe.create.error", e.getMessage()), e);
        }
    }

    @Override
    public RecipeDto updateRecipe(Long id, String title, Long categoryId,
                                  List<String> ingredients, String description,
                                  List<Long> inventoryIds, boolean published) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("recipe.not_found", id)
                ));

        CategoryDto categoryDto = categoryService.getCategoryEntityForInternalUse(categoryId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("category.not_found", categoryId)
                ));

        Category category = createCategoryEntityFromDto(categoryDto);

        recipe.setTitle(title);
        recipe.setCategory(category);
        recipe.setDescription(description);
        recipe.setPublished(published);

        recipe.getIngredients().clear();
        if (ingredients != null) {
            recipe.getIngredients().addAll(ingredients);
        }

        recipe.getInventoryItems().clear();
        if (inventoryIds != null && !inventoryIds.isEmpty()) {
            List<InventoryDto> inventoryDtos = inventoryService.getInventoryByIdsForInternalUse(inventoryIds);
            for (InventoryDto inventoryDto : inventoryDtos) {
                Inventory inventory = createInventoryEntityFromDto(inventoryDto);
                recipe.addInventoryItem(inventory);
            }
        }

        Recipe savedRecipe = recipeRepository.save(recipe);
        return recipeConverter.toDto(savedRecipe);
    }

    private Category createCategoryEntityFromDto(CategoryDto categoryDto) {
        if (categoryDto == null) {
            throw new IllegalArgumentException(messageProvider.getMessage("category.dto.null"));
        }

        Category category = new Category();
        category.setId(categoryDto.id());
        category.setName(categoryDto.name());
        category.setDescription(categoryDto.description());
        return category;
    }

    private Author createAuthorEntityFromDto(AuthorDto authorDto) {
        if (authorDto == null) {
            throw new IllegalArgumentException(messageProvider.getMessage("author.dto.null"));
        }
        if (authorDto.user() == null) {
            throw new IllegalArgumentException(messageProvider.getMessage("author.user.null"));
        }

        Author author = new Author();
        author.setId(authorDto.id());

        User user = new User();
        user.setId(authorDto.user().id());
        user.setUsername(authorDto.user().username());
        user.setEmail(authorDto.user().email());
        user.setEnabled(authorDto.user().enabled());

        if (authorDto.user().roles() != null) {
            user.setRoles(new HashSet<>(authorDto.user().roles()));
        }

        author.setUser(user);
        author.setBio(authorDto.bio());
        return author;
    }

    private Inventory createInventoryEntityFromDto(InventoryDto inventoryDto) {
        if (inventoryDto == null) {
            throw new IllegalArgumentException(messageProvider.getMessage("inventory.dto.null"));
        }

        Inventory inventory = new Inventory();
        inventory.setId(inventoryDto.id());
        inventory.setName(inventoryDto.name());
        inventory.setDescription(inventoryDto.description());
        return inventory;
    }

    @Override
    public Optional<RecipeDto> getRecipeById(Long id) {
        return recipeRepository.findByIdWithBasicRelations(id)
                .map(recipeConverter::toDto);
    }

    @Override
    public Optional<RecipeDto> getRecipeByIdWithAllRelations(Long id) {
        return Optional.of(getRecipeWithDetails(id))
                .map(recipeConverter::toDtoFromDetails);
    }

    @Override
    public List<RecipeSummaryDto> getAllPublishedRecipes() {
        return recipeRepository.findPublishedRecipesWithBasicAssociations().stream()
                .map(recipeConverter::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecipeSummaryDto> getRecipesByAuthor(Long authorId) {
        return recipeRepository.findByAuthorIdWithDetails(authorId).stream()
                .map(recipeConverter::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecipeSummaryDto> getPublishedRecipesByAuthor(Long authorId) {
        return recipeRepository.findPublishedByAuthorIdWithDetails(authorId).stream()
                .map(recipeConverter::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecipeSummaryDto> searchPublishedRecipesByTitle(String title) {
        return recipeRepository.findPublishedByTitleContainingIgnoreCase(title).stream()
                .map(recipeConverter::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecipeSummaryDto> searchPublishedRecipesByIngredient(String ingredient) {
        return recipeRepository.findPublishedByIngredientContaining(ingredient).stream()
                .map(recipeConverter::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecipeSummaryDto> findPublishedRecipesByFilters(String title, Long categoryId, Long authorId) {
        return recipeRepository.findPublishedByFilters(title, categoryId, authorId).stream()
                .map(recipeConverter::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecipeDto> findPublishedRecipesWithFilters(String search, Long categoryId, Long authorId) {
        List<Recipe> recipes;

        if (search != null && !search.trim().isEmpty()) {
            recipes = recipeRepository.findPublishedByTitleContainingIgnoreCase(search.trim());
        } else if (categoryId != null || authorId != null) {
            recipes = recipeRepository.findPublishedByFilters(null, categoryId, authorId);
        } else {
            recipes = recipeRepository.findPublishedRecipesWithBasicAssociations();
        }

        return recipes.stream()
                .map(recipeConverter::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteRecipe(Long id) {
        if (!recipeRepository.existsById(id)) {
            throw new EntityNotFoundException(messageProvider.getMessage("recipe.not_found", id));
        }

        commentRepository.deleteByRecipeId(id);
        recipeRepository.deleteInventoryAssociations(id);
        recipeRepository.deleteIngredients(id);
        recipeRepository.deleteById(id);
    }

    @Override
    public RecipeDto publishRecipe(Long id) {
        Recipe recipe = recipeRepository.findByIdWithBasicRelations(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("recipe.not_found", id)
                ));

        recipe.publish();
        Recipe publishedRecipe = recipeRepository.save(recipe);
        return recipeConverter.toDto(publishedRecipe);
    }

    @Override
    public RecipeDto unpublishRecipe(Long id) {
        Recipe recipe = recipeRepository.findByIdWithBasicRelations(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageProvider.getMessage("recipe.not_found", id)
                ));

        recipe.unpublish();
        Recipe unpublishedRecipe = recipeRepository.save(recipe);
        return recipeConverter.toDto(unpublishedRecipe);
    }

    @Override
    public List<RecipeSummaryDto> getRecentPublishedRecipes(int limit) {
        return recipeRepository.findRecentPublishedRecipes(limit).stream()
                .map(recipeConverter::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecipeSummaryDto> getPopularPublishedRecipes(int limit) {
        return recipeRepository.findPopularPublishedRecipes(limit).stream()
                .map(recipeConverter::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    public long getPublishedRecipesCount() {
        return recipeRepository.countByPublishedTrue();
    }

    @Override
    public List<InventoryDto> getInventoryByRecipeId(Long recipeId) {
        return inventoryService.getInventoryByRecipeId(recipeId);
    }

    @Override
    public boolean isInventoryUsedInRecipes(Long inventoryId) {
        return inventoryService.isInventoryUsedInRecipes(inventoryId);
    }

    @Override
    public long getRecipeCountByInventoryId(Long inventoryId) {
        return inventoryService.getRecipeCountByInventory(inventoryId);
    }
}