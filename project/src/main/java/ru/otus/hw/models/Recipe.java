package ru.otus.hw.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "recipes")
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @ToString.Exclude
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    @ToString.Exclude
    private Author author;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "recipe_inventory",
            joinColumns = @JoinColumn(name = "recipe_id"),
            inverseJoinColumns = @JoinColumn(name = "inventory_id")
    )
    @ToString.Exclude
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
    @ToString.Exclude
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
        this.inventoryItems = new ArrayList<>();
        this.ingredients = new ArrayList<>();
        this.comments = new ArrayList<>();
        this.published = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Recipe(String title, Category category, Author author, List<String> ingredients, String description) {
        this.title = title;
        this.category = category;
        this.author = author;
        this.ingredients = ingredients != null ? ingredients : new ArrayList<>();
        this.description = description;
        this.inventoryItems = new ArrayList<>();
        this.comments = new ArrayList<>();
        this.published = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void addInventoryItem(Inventory inventory) {
        this.inventoryItems.add(inventory);
        inventory.getRecipes().add(this);
    }

    public void removeInventoryItem(Inventory inventory) {
        this.inventoryItems.remove(inventory);
        inventory.getRecipes().remove(this);
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

    public boolean isPublished() {
        return published;
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
