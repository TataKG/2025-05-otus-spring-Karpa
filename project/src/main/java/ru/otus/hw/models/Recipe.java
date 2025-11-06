package ru.otus.hw.models;

import jakarta.persistence.*;
import lombok.*;

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
    @JoinColumn(name = "category_id")
    @ToString.Exclude
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
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
    @CollectionTable(name = "recipe_ingredients", joinColumns = @JoinColumn(name = "recipe_id"))
    @Column(name = "ingredient")
    private List<String> ingredients = new ArrayList<>();

    @Lob
    @Column(nullable = false)
    private String description;

    @OneToMany(mappedBy = "recipe", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Comment> comments = new ArrayList<>();

    public Recipe(String title, Category category, Author author, String description) {
        this.title = title;
        this.category = category;
        this.author = author;
        this.description = description;
    }

    public Recipe(String title, Category category, Author author, List<String> ingredients, String description) {
        this.title = title;
        this.category = category;
        this.author = author;
        this.ingredients = ingredients != null ? ingredients : new ArrayList<>();
        this.description = description;
    }

    public void addInventoryItem(Inventory inventory) {
        this.inventoryItems.add(inventory);
        inventory.getRecipes().add(this);
    }

    public void addIngredient(String ingredient) {
        this.ingredients.add(ingredient);
    }
}
