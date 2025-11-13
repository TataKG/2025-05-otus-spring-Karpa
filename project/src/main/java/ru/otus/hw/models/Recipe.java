package ru.otus.hw.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "recipes")
@NamedEntityGraphs({
        @NamedEntityGraph(
                name = "Recipe.withBasicRelations",
                attributeNodes = {
                        @NamedAttributeNode("category"),
                        @NamedAttributeNode("author")
                }
        ),
        @NamedEntityGraph(
                name = "Recipe.withAllRelations",
                attributeNodes = {
                        @NamedAttributeNode("category"),
                        @NamedAttributeNode("author"),
                        @NamedAttributeNode("inventoryItems")
                }
        ),
        @NamedEntityGraph(
                name = "Recipe.withCategoryAndAuthor",
                attributeNodes = {
                        @NamedAttributeNode("category"),
                        @NamedAttributeNode("author")
                }
        ),
        @NamedEntityGraph(
                name = "Recipe.withCommentsAndUser",
                attributeNodes = {
                        @NamedAttributeNode("category"),
                        @NamedAttributeNode("author"),
                        @NamedAttributeNode("comments")
                }
        ),
        @NamedEntityGraph(
                name = "Recipe.withInventoryAndIngredients",
                attributeNodes = {
                        @NamedAttributeNode("category"),
                        @NamedAttributeNode("author"),
                        @NamedAttributeNode("inventoryItems"),
                        @NamedAttributeNode("ingredients")
                }
        )
})
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "recipe_inventory",
            joinColumns = @JoinColumn(name = "recipe_id"),
            inverseJoinColumns = @JoinColumn(name = "inventory_id")
    )
    private List<Inventory> inventoryItems = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "recipe_ingredients",
            joinColumns = @JoinColumn(name = "recipe_id")
    )
    @Column(name = "ingredient")
    @OrderColumn(name = "ingredient_order")
    private List<String> ingredients = new ArrayList<>();

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @OneToMany(
            mappedBy = "recipe",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<Comment> comments = new ArrayList<>();

    @Column(nullable = false)
    private boolean published = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Recipe(String title, Category category, Author author, String description) {
        this.title = title;
        this.category = category;
        this.author = author;
        this.description = description;
        this.published = false;
    }

    public Recipe(String title, Category category, Author author, List<String> ingredients, String description) {
        this.title = title;
        this.category = category;
        this.author = author;
        this.ingredients = ingredients != null ? ingredients : new ArrayList<>();
        this.description = description;
        this.published = false;
    }

    public void addInventoryItem(Inventory inventory) {
        this.inventoryItems.add(inventory);
    }

    public void removeInventoryItem(Inventory inventory) {
        this.inventoryItems.remove(inventory);
    }

    public void addIngredient(String ingredient) {
        this.ingredients.add(ingredient);
    }

    public void addComment(Comment comment) {
        this.comments.add(comment);
        comment.setRecipe(this);
    }

    public void removeComment(Comment comment) {
        this.comments.remove(comment);
        comment.setRecipe(null);
    }

    public void publish() {
        this.published = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void unpublish() {
        this.published = false;
        this.updatedAt = LocalDateTime.now();
    }
}
